const fs = require('fs');
let html = fs.readFileSync('admin.html', 'utf8');

// Replace the Library content to group by category
const newLibraryHtml = `
        <main class="flex-1 overflow-hidden p-4 md:p-6 relative z-10 flex flex-col gap-6" v-if="currentTab === 'library'">
            <div class="glass-panel p-6 rounded-3xl w-full max-w-5xl mx-auto h-full flex flex-col">
                <h2 class="text-xl font-bold text-white mb-4">Content Library</h2>
                <div class="overflow-y-auto custom-scrollbar flex-1 pr-2 space-y-8 pb-24 md:pb-0">
                    <div v-if="Object.keys(groupedItems).length === 0" class="text-center py-10 text-slate-500">
                        No content found.
                    </div>
                    
                    <div v-for="(group, category) in groupedItems" :key="category" class="space-y-4">
                        <h3 class="text-lg font-bold text-orange-400 border-b border-white/10 pb-2">{{ category }}</h3>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div v-for="item in group" :key="item.key" class="flex gap-4 p-3 bg-black/40 border border-white/5 rounded-2xl items-center hover:border-white/20 transition-colors">
                                <img :src="item.posterUrl" class="w-16 h-24 object-cover rounded-xl shadow-lg">
                                <div class="flex-1">
                                    <div class="flex items-center gap-2">
                                        <h3 class="font-bold text-white text-lg line-clamp-1">{{ item.title }}</h3>
                                        <span v-if="item.isHero" class="bg-orange-500 text-white text-[10px] font-bold px-2 py-0.5 rounded tracking-widest shrink-0">HERO</span>
                                    </div>
                                    <p class="text-xs text-slate-400">{{ item.year }} • <span class="uppercase text-purple-400 font-bold">{{ item.type }}</span></p>
                                    <p class="text-xs text-slate-500 mt-1 line-clamp-1">{{ item.description }}</p>
                                </div>
                                <div class="flex flex-col gap-2">
                                    <button @click="editContent(item.key, item)" class="w-8 h-8 flex items-center justify-center rounded-full bg-blue-500/10 text-blue-400 hover:bg-blue-500/20 active:scale-95 transition-all">
                                        <span class="material-symbols-outlined text-[16px]">edit</span>
                                    </button>
                                    <button @click="deleteContent(item.key)" class="w-8 h-8 flex items-center justify-center rounded-full bg-red-500/10 text-red-500 hover:bg-red-500/20 active:scale-95 transition-all">
                                        <span class="material-symbols-outlined text-[16px]">delete</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
`;

html = html.replace(/<main class="flex-1 overflow-hidden p-4 md:p-6 relative z-10 flex flex-col gap-6" v-if="currentTab === 'library'">[\s\S]*?<\/main>/, newLibraryHtml);

// Add groupedItems computed property
const groupedItemsCode = `
                const groupedItems = computed(() => {
                    const groups = {};
                    Object.entries(items.value).forEach(([key, item]) => {
                        let mainCat = 'Uncategorized';
                        if (item.category) {
                            mainCat = item.category.split(',')[0].trim();
                        }
                        if (!groups[mainCat]) groups[mainCat] = [];
                        groups[mainCat].push({ ...item, key });
                    });
                    
                    // Sort keys
                    return Object.keys(groups).sort().reduce((acc, key) => {
                        acc[key] = groups[key];
                        return acc;
                    }, {});
                });
`;

html = html.replace(/const allCategories = computed/, groupedItemsCode + '\n                const allCategories = computed');
html = html.replace(/form,\n *allCategories,/, 'form,\n                    groupedItems,\n                    allCategories,');

fs.writeFileSync('admin.html', html);
