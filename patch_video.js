const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', 'utf8');

code = code.replace(/androidx\.compose\.animation\.AnimatedVisibility\([\s\S]*?\n            \}[\s\S]*?\}\n            \}\n            \}/g, '');
code = code.replace(/\/\/ Volume Indicator[\s\S]*?\}\n            \}\n            \}/g, '');
code = code.replace(/\/\/ Volume Indicator[\s\S]*?\}\n            \}/g, '');

const regexVolume = /\/\/ Volume Indicator[\s\S]*?\}\n            \}/g;
code = code.replace(regexVolume, '');

const regexBrightness = /\/\/ Brightness Indicator[\s\S]*?\}\n            \}/g;
code = code.replace(regexBrightness, '');

fs.writeFileSync('app/src/main/java/com/example/ui/VideoPlayerScreen.kt', code);
