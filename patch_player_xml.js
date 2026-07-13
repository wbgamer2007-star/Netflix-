const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', 'utf8');

code = code.replace(/useController = false/, `useController = true\n                    // setShowSubtitleButton(true) \n                    // custom layout not needed if we use standard but we can use R.layout.custom_exo_playback_control_view`);

fs.writeFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', code);
