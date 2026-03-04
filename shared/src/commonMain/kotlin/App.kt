import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.launch
import kotlinx.io.buffered

@Composable
fun App() {
    MaterialTheme {
        var uploading by remember { mutableStateOf(false) }
        var uploadedBytes by remember { mutableStateOf(0L) }
        var totalBytes by remember { mutableStateOf(0L) }
        var status by remember { mutableStateOf("Idle") }
        var heartbeat by remember { mutableStateOf(0) }

        val scope = rememberCoroutineScope()

        // Heartbeat counter - if UI freezes, this stops incrementing
        LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(16)
                heartbeat++
            }
        }

        val picker = rememberFilePickerLauncher { file: PlatformFile? ->
            if (file != null) {
                scope.launch {
                    uploading = true
                    uploadedBytes = 0L
                    totalBytes = file.size()
                    status = "Uploading ${file.name} to httpbin.org..."

                    try {
                        val client = HttpClient()

                        val response = client.submitFormWithBinaryData(
                            url = "https://httpbin.org/post",
                            formData = formData {
                                append("description", "FileKit upload test")
                                append(
                                    "file",
                                    InputProvider { file.source().buffered() },
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "application/octet-stream")
                                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                    }
                                )
                            }
                        ) {
                            onUpload { bytesSentTotal, contentLength ->
                                uploadedBytes = bytesSentTotal
                                totalBytes = contentLength ?: file.size()
                            }
                        }

                        status = "Done! Server responded: ${response.status}"
                        client.close()
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    }

                    uploading = false
                }
            }
        }

        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "FileKit UI Freeze Repro",
                    style = MaterialTheme.typography.headlineSmall,
                )

                // Spinning indicator - freezes if main thread is blocked
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Spinner should keep spinning (heartbeat: $heartbeat)")
                }

                Button(
                    onClick = { picker.launch() },
                    enabled = !uploading,
                ) {
                    Text(if (uploading) "Uploading..." else "Pick a file")
                }

                Text("Status: $status")

                if (totalBytes > 0) {
                    Text("Progress: $uploadedBytes / $totalBytes bytes")
                    LinearProgressIndicator(
                        progress = { uploadedBytes.toFloat() / totalBytes.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else if (uploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
