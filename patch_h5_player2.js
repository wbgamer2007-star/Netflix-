const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', 'utf8');

const regex = /AndroidView\([\s\S]*?factory = \{ ctx ->[\s\S]*?PlayerView\(ctx\)\.apply \{[\s\S]*?player = exoPlayer[\s\S]*?useController = true[\s\S]*?layoutParams = FrameLayout\.LayoutParams\([\s\S]*?ViewGroup\.LayoutParams\.MATCH_PARENT,[\s\S]*?ViewGroup\.LayoutParams\.MATCH_PARENT[\s\S]*?\)[\s\S]*?keepScreenOn = true[\s\S]*?\n[\s\S]*?\}\n[\s\S]*?\}\n[\s\S]*?\}\n[\s\S]*?\)/;

const replacement = `AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        keepScreenOn = true
                        
                        val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                            override fun onScroll(e1: android.view.MotionEvent?, e2: android.view.MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                                if (e1 == null) return false
                                val deltaX = e2.x - e1.x
                                val deltaY = e2.y - e1.y
                                
                                if (kotlin.math.abs(deltaY) > kotlin.math.abs(deltaX)) {
                                    // Vertical scroll (Volume / Brightness)
                                    if (e1.x < width / 2) {
                                        // Left side -> Brightness
                                        val layoutParams = activity?.window?.attributes
                                        layoutParams?.screenBrightness = ((layoutParams?.screenBrightness ?: 0.5f) + (deltaY / height)).coerceIn(0f, 1f)
                                        activity?.window?.attributes = layoutParams
                                        brightnessLevel = layoutParams?.screenBrightness ?: 0.5f
                                        showBrightnessIndicator = true
                                    } else {
                                        // Right side -> Volume
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
                            false // let Compose handle clicks if needed
                        }
                    }
                }
            )

            H5PlayerControls(
                player = exoPlayer,
                title = movie?.title ?: "Video",
                onBack = onNavigateUp,
                onShowSettings = {
                    val builder = androidx.media3.ui.TrackSelectionDialogBuilder(
                        context, "Tracks", exoPlayer, androidx.media3.common.C.TRACK_TYPE_AUDIO
                    )
                    builder.build().show()
                },
                modifier = Modifier.fillMaxSize()
            )`;

code = code.replace(regex, replacement);

code = code.replace(/if \(\!isFullscreen\) \{\n                IconButton\([\s\S]*?onClick = onNavigateUp,[\s\S]*?Modifier\.align\(Alignment\.TopStart\)\.padding\(8\.dp\)[\s\S]*?\{\n                Icon\([\s\S]*?imageVector = Icons\.AutoMirrored\.Filled\.ArrowBack,[\s\S]*?contentDescription = "Back",[\s\S]*?tint = Color\.White[\s\S]*?\)[\s\S]*?\}[\s\S]*?\}/, '');

fs.writeFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', code);
