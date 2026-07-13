const fs = require('fs');
let html = fs.readFileSync('admin.html', 'utf8');

html = html.replace(/<nav class="flex items-center gap-6">[\s\S]*?<\/nav>/g, `
<nav class="hidden md:flex items-center gap-6">
    <button @click="currentTab = 'add'" :class="currentTab === 'add' ? 'text-white font-bold' : 'text-slate-400 hover:text-white'">Add Content</button>
    <button @click="currentTab = 'library'" :class="currentTab === 'library' ? 'text-white font-bold' : 'text-slate-400 hover:text-white'">Library</button>
    <button @click="currentTab = 'settings'" :class="currentTab === 'settings' ? 'text-white font-bold' : 'text-slate-400 hover:text-white'">Settings</button>
</nav>
`);

html = html.replace(/<main class="flex-1 overflow-hidden p-6 relative z-10 flex flex-col gap-6" v-if="currentTab === 'content'">[\s\S]*?<\/main>/g, `
        <main class="flex-1 overflow-hidden p-4 md:p-6 relative z-10 flex flex-col gap-6" v-if="currentTab === 'add'">
            <div class="glass-panel p-6 rounded-3xl w-full max-w-3xl mx-auto h-full overflow-y-auto custom-scrollbar flex flex-col gap-4">
                <h2 class="text-xl font-bold text-white mb-2">{{ editingKey ? 'Edit Content' : 'Add New Content' }}</h2>
                <div class="flex justify-between items-center mb-2" v-if="editingKey">
                    <span class="text-xs text-orange-400 font-bold">Editing existing item</span>
                    <button @click="cancelEdit" class="text-xs text-slate-400 hover:text-white underline">Cancel</button>
                </div>
                
                <div class="flex gap-2 p-1 bg-black/40 rounded-xl">
                    <button @click="form.type = 'movie'" :class="{'bg-orange-500 text-white shadow': form.type === 'movie', 'text-slate-400': form.type !== 'movie'}" class="flex-1 py-2 rounded-lg font-bold text-sm transition-all">Movie</button>
                    <button @click="form.type = 'series'" :class="{'bg-orange-500 text-white shadow': form.type === 'series', 'text-slate-400': form.type !== 'series'}" class="flex-1 py-2 rounded-lg font-bold text-sm transition-all">Series</button>
                </div>

                <div>
                    <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Title</label>
                    <input type="text" v-model="form.title" class="w-full bg-black/40 text-white rounded-xl p-3 border border-white/10 focus:border-orange-500 focus:outline-none">
                </div>

                <div>
                    <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Description</label>
                    <textarea v-model="form.description" rows="3" class="w-full bg-black/40 text-white rounded-xl p-3 border border-white/10 focus:border-orange-500 focus:outline-none"></textarea>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <div class="col-span-2">
                        <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Categories (Comma separated)</label>
                        <input type="text" v-model="form.category" placeholder="e.g. Sci-Fi, Action" class="w-full bg-black/40 text-white rounded-xl p-3 border border-white/10 focus:border-orange-500 focus:outline-none mb-2">
                        <div class="flex flex-wrap gap-2">
                            <button v-for="cat in allCategories" :key="cat" @click.prevent="toggleCategory(cat)" 
                                    :class="{'bg-orange-500 text-white border-orange-500': hasCategory(cat), 'bg-transparent text-slate-400 border-white/20': !hasCategory(cat)}"
                                    class="text-xs px-2 py-1 border rounded-lg hover:border-orange-500 transition-colors">
                                {{ cat }}
                            </button>
                        </div>
                    </div>
                    <div class="col-span-2">
                        <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Year</label>
                        <input type="number" v-model="form.year" class="w-full bg-black/40 text-white rounded-xl p-3 border border-white/10 focus:border-orange-500 focus:outline-none">
                    </div>
                </div>

                <div>
                    <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Poster Image (URL or Upload)</label>
                    <input type="text" v-model="form.posterUrl" placeholder="https://..." class="w-full bg-black/40 text-white rounded-xl p-3 border border-white/10 focus:border-orange-500 focus:outline-none mb-2">
                    <div class="flex items-center gap-2">
                        <input type="file" @change="uploadImage" accept="image/*" class="text-xs text-slate-400" id="fileUpload">
                        <span v-if="uploadingImage" class="text-xs text-orange-500 animate-pulse">Uploading...</span>
                    </div>
                </div>

                <div v-if="form.type === 'movie'">
                    <label class="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Video Stream URL</label>
                    <input type="text" v-model="form.videoUrl" class="w-full bg-black/40 text-white rounded-xl p-3 border border-white/10 focus:border-orange-500 focus:outline-none">
                </div>

                <div v-if="form.type === 'series'" class="border-t border-white/10 pt-4 mt-2 space-y-4">
                    <div class="flex justify-between items-center">
                        <label class="block text-xs font-bold uppercase tracking-wider text-slate-400">Seasons & Episodes</label>
                        <button @click.prevent="addSeason" class="text-xs bg-white/10 hover:bg-white/20 px-2 py-1 rounded text-white">+ Add Season</button>
                    </div>
                    
                    <div v-for="(season, sIdx) in form.seasons" :key="sIdx" class="bg-black/30 p-3 rounded-xl border border-white/5 space-y-3">
                        <div class="flex gap-2">
                            <input v-model="season.title" placeholder="Season Title" class="flex-1 bg-black/40 text-white text-sm rounded-lg p-2 border border-white/10 focus:border-orange-500 focus:outline-none">
                            <button @click.prevent="removeSeason(sIdx)" class="text-red-400 px-2 hover:text-red-300">X</button>
                        </div>
                        
                        <div class="space-y-2 pl-4 border-l-2 border-white/10">
                            <div v-for="(episode, eIdx) in season.episodes" :key="eIdx" class="flex flex-col gap-1 bg-black/20 p-2 rounded-lg">
                                <div class="flex justify-between">
                                    <span class="text-xs font-bold text-slate-400">Episode {{ eIdx + 1 }}</span>
                                    <button @click.prevent="removeEpisode(sIdx, eIdx)" class="text-red-400 text-xs">Remove</button>
                                </div>
                                <input v-model="episode.title" placeholder="Episode Title" class="w-full bg-black/40 text-white text-sm rounded-lg p-2 border border-white/10">
                                <input v-model="episode.videoUrl" placeholder="Video URL" class="w-full bg-black/40 text-white text-sm rounded-lg p-2 border border-white/10">
                            </div>
                            <button @click.prevent="addEpisode(sIdx)" class="text-xs text-orange-400 hover:text-orange-300 font-bold">+ Add Episode</button>
                        </div>
                    </div>
                </div>

                <div class="flex items-center gap-2 mt-2">
                    <input type="checkbox" v-model="form.isHero" id="isHero" class="h-4 w-4 rounded accent-orange-500">
                    <label for="isHero" class="text-sm font-bold text-slate-300">Set as Hero Banner</label>
                </div>

                <button @click="saveContent" class="mt-4 w-full bg-white text-black font-bold py-3 rounded-xl hover:bg-slate-200 transition-colors active:scale-95">{{ editingKey ? 'Update Database' : 'Save to Database' }}</button>
            </div>
        </main>

        <main class="flex-1 overflow-hidden p-4 md:p-6 relative z-10 flex flex-col gap-6" v-if="currentTab === 'library'">
            <div class="glass-panel p-6 rounded-3xl w-full max-w-4xl mx-auto h-full flex flex-col">
                <h2 class="text-xl font-bold text-white mb-4">Content Library</h2>
                <div class="overflow-y-auto custom-scrollbar flex-1 pr-2 space-y-3 pb-24 md:pb-0">
                    <div v-for="(item, key) in items" :key="key" class="flex gap-4 p-3 bg-black/40 border border-white/5 rounded-2xl items-center hover:border-white/20 transition-colors">
                        <img :src="item.posterUrl" class="w-16 h-24 object-cover rounded-xl shadow-lg">
                        <div class="flex-1">
                            <div class="flex items-center gap-2">
                                <h3 class="font-bold text-white text-lg">{{ item.title }}</h3>
                                <span v-if="item.isHero" class="bg-orange-500 text-white text-[10px] font-bold px-2 py-0.5 rounded tracking-widest">HERO</span>
                            </div>
                            <p class="text-xs text-slate-400">{{ item.year }} • {{ item.category }} • <span class="uppercase text-purple-400 font-bold">{{ item.type }}</span></p>
                            <p class="text-xs text-slate-500 mt-1 line-clamp-1">{{ item.description }}</p>
                        </div>
                        <button @click="editContent(key, item)" class="w-10 h-10 flex items-center justify-center rounded-full bg-blue-500/10 text-blue-400 hover:bg-blue-500/20 active:scale-95 transition-all">
                            <span class="material-symbols-outlined text-[20px]">edit</span>
                        </button>
                        <button @click="deleteContent(key)" class="w-10 h-10 flex items-center justify-center rounded-full bg-red-500/10 text-red-500 hover:bg-red-500/20 active:scale-95 transition-all">
                            <span class="material-symbols-outlined text-[20px]">delete</span>
                        </button>
                    </div>
                    <div v-if="Object.keys(items).length === 0" class="text-center py-10 text-slate-500">
                        No content found.
                    </div>
                </div>
            </div>
        </main>
`);

// Add bottom nav before closing body
html = html.replace('</body>', `
        <!-- Bottom Nav for Mobile -->
        <div class="md:hidden fixed bottom-0 left-0 right-0 bg-[#0a0a0f]/90 backdrop-blur-xl border-t border-white/10 flex justify-around p-4 z-50">
            <button @click="currentTab = 'add'" :class="currentTab === 'add' ? 'text-orange-500' : 'text-slate-400'" class="flex flex-col items-center">
                <span class="material-symbols-outlined mb-1">add_circle</span>
                <span class="text-[10px] font-bold">Add</span>
            </button>
            <button @click="currentTab = 'library'" :class="currentTab === 'library' ? 'text-orange-500' : 'text-slate-400'" class="flex flex-col items-center">
                <span class="material-symbols-outlined mb-1">video_library</span>
                <span class="text-[10px] font-bold">Library</span>
            </button>
            <button @click="currentTab = 'settings'" :class="currentTab === 'settings' ? 'text-orange-500' : 'text-slate-400'" class="flex flex-col items-center">
                <span class="material-symbols-outlined mb-1">settings</span>
                <span class="text-[10px] font-bold">Settings</span>
            </button>
        </div>
    </div>
</body>`);

// update default tab
html = html.replace("const currentTab = ref('content');", "const currentTab = ref('add');");

// Edit content redirects to Add tab
html = html.replace(`const editContent = (key, item) => {
                    editingKey.value = key;
                    form.value = { ...defaultForm(), ...JSON.parse(JSON.stringify(item)) };
                };`, `const editContent = (key, item) => {
                    editingKey.value = key;
                    form.value = { ...defaultForm(), ...JSON.parse(JSON.stringify(item)) };
                    currentTab.value = 'add';
                };`);

// Update content redirects to library tab
html = html.replace(`editingKey.value = null;
                            document.getElementById('fileUpload').value = '';
                        }).catch(err => alert("Error updating: " + err));`, `editingKey.value = null;
                            document.getElementById('fileUpload').value = '';
                            currentTab.value = 'library';
                        }).catch(err => alert("Error updating: " + err));`);

html = html.replace(`form.value = defaultForm();
                            document.getElementById('fileUpload').value = '';
                        }).catch(err => alert("Error saving: " + err));`, `form.value = defaultForm();
                            document.getElementById('fileUpload').value = '';
                            currentTab.value = 'library';
                        }).catch(err => alert("Error saving: " + err));`);

fs.writeFileSync('admin.html', html);
