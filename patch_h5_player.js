const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', 'utf8');

// replace the AndroidView
const oldAndroidView = `                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            // setShowSubtitleButton(true) 
                            // custom layout not needed if we use standard but we can use R.layout.custom_exo_playback_control_view
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )`;

const newAndroidView = `                AndroidView(
                    factory = { ctx ->
                        androidx.media3.ui.PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                H5PlayerControls(
                    player = exoPlayer,
                    title = movie?.title ?: "Video",
                    onBack = onNavigateUp,
                    onShowSettings = {
                        // In a real app we would have a nice Compose dialog, 
                        // but here we can use ExoPlayer's built in track selector
                        val builder = androidx.media3.ui.TrackSelectionDialogBuilder(
                            context, "Audio/Subtitle", exoPlayer, androidx.media3.common.C.TRACK_TYPE_AUDIO
                        )
                        builder.build().show()
                    },
                    modifier = Modifier.fillMaxSize()
                )`;

code = code.replace(oldAndroidView, newAndroidView);
code = code.replace(/import androidx.compose.material.icons.filled.ArrowBack/, "import androidx.compose.material.icons.filled.ArrowBack\nimport android.widget.FrameLayout");

fs.writeFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', code);
