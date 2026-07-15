const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', 'utf8');

const regex = /\/\/ Player Section[\s\S]*?\/\/ Details Section/g;

const replacement = `// Player Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.aspectRatio(16f / 9f))
                .background(Color.Black)
                .statusBarsPadding()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = android.widget.FrameLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        keepScreenOn = true
                        
                        val gestureDetector = android.view.GestureDetector(ctx, object : android.view.GestureDetector.SimpleOnGestureListener() {
                            override fun onScroll(e1: android.view.MotionEvent?, e2: android.view.MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                                if (e1 == null) return false
                                val deltaX = e2.x - e1.x
                                val deltaY = e2.y - e1.y
                                
                                if (kotlin.math.abs(deltaY) > kotlin.math.abs(deltaX)) {
                                    if (e1.x < width / 2) {
                                        val layoutParams = activity?.window?.attributes
                                        layoutParams?.screenBrightness = ((layoutParams?.screenBrightness ?: 0.5f) + (deltaY / height)).coerceIn(0f, 1f)
                                        activity?.window?.attributes = layoutParams
                                        brightnessLevel = layoutParams?.screenBrightness ?: 0.5f
                                        showBrightnessIndicator = true
                                    } else {
                                        val audioManager = ctx.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                                        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                                        val currentVol = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
                                        val newVolume = (currentVol + (deltaY / height) * maxVolume).toInt().coerceIn(0, maxVolume)
                                        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVolume, 0)
                                        volumeLevel = newVolume.toFloat() / maxVolume
                                        showVolumeIndicator = true
                                    }
                                }
                                return true
                            }
                        })
                        
                        setOnTouchListener { _, event ->
                            gestureDetector.onTouchEvent(event)
                            false
                        }
                    }
                }
            )

            H5PlayerControls(
                player = exoPlayer,
                title = movie?.title ?: "Video",
                onBack = {
                    if (isFullscreen) {
                        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        isFullscreen = false
                    } else {
                        onNavigateUp()
                    }
                },
                onShowSettings = {
                    val builder = androidx.media3.ui.TrackSelectionDialogBuilder(
                        context, "Tracks", exoPlayer, androidx.media3.common.C.TRACK_TYPE_AUDIO
                    )
                    builder.build().show()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = com.example.ui.theme.AccentOrange
                )
            }

            VolumeIndicator(
                show = showVolumeIndicator,
                volumeLevel = volumeLevel,
                modifier = Modifier.align(Alignment.CenterEnd).padding(32.dp)
            )
            
            BrightnessIndicator(
                show = showBrightnessIndicator,
                brightnessLevel = brightnessLevel,
                modifier = Modifier.align(Alignment.CenterStart).padding(32.dp)
            )
        }

        // Details Section`;

code = code.replace(regex, replacement);
fs.writeFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', code);
