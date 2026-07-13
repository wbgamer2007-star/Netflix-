const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/MainScreen.kt', 'utf8');

code = code.replace(/                1 -> \{\n                    HistoryScreen[\s\S]*?\n                \}\n                2 -> \{\n                    ProfileScreen[\s\S]*?\n                \}\n                    HistoryScreen[\s\S]*?\n                \}/, `                1 -> {
                    HistoryScreen(onNavigateToPlayer, onNavigateToSeries)
                }
                2 -> {
                    ProfileScreen(onNavigateToPlayer, onNavigateToSeries)
                }`);

fs.writeFileSync('app/src/main/java/com/example/ui/MainScreen.kt', code);
