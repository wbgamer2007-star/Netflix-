const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', 'utf8');

// replace the AndroidView for PlayerView to disable useController
code = code.replace(/useController = true/, 'useController = false');

fs.writeFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', code);
