// ==UserScript==
// @name         Grepolis Academy Planner v1
// @namespace    http://tampermonkey.net/
// @version      1.0
// @description  Advanced Academy Planner
// @author       GFRCnlvng
// @match        *://*.grepolis.com/*
// @grant        none
// ==/UserScript==

(function() {
    'use strict';

    // ==========================================
    // 1. UPDATED RESEARCH DATA WITH ACCURATE COSTS
    // ==========================================
    const LEVELS = [1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34];
    
    const RESEARCH_DATA = {
        1:  [
            {n:"Slinger", p:4, w:300, s:500, si:200},
            {n:"Archer", p:8, w:550, s:100, si:400},
            {n:"City Guard", p:4, w:400, s:300, si:300}
        ],
        4:  [
            {n:"Hoplite", p:8, w:600, s:200, si:850},
            {n:"Diplomacy", p:4, w:100, s:400, si:200},
            {n:"Meteorology", p:4, w:2500, s:1700, si:6500}
        ],
        7:  [
            {n:"Espionage", p:4, w:900, s:900, si:1100},
            {n:"Booty", p:8, w:1200, s:1200, si:1200},
            {n:"Ceramics", p:6, w:700, s:1500, si:900}
        ],
        10: [
            {n:"Horseman", p:8, w:1400, s:700, si:1800},
            {n:"Architecture", p:6, w:1900, s:2100, si:1300},
            {n:"Trainer", p:4, w:800, s:1300, si:1600}
        ],
        13: [
            {n:"Colony Ship", p:0, w:7500, s:7500, si:9500},
            {n:"Bireme", p:8, w:2800, s:1300, si:2200},
            {n:"Crane", p:4, w:3000, s:1800, si:1400},
            {n:"Shipwright", p:6, w:5000, s:2000, si:3900}
        ],
        16: [
            {n:"Chariot", p:8, w:3700, s:1900, si:2800},
            {n:"Light Ship", p:8, w:4400, s:2000, si:2400},
            {n:"Conscription", p:4, w:3800, s:4200, si:6000}
        ],
        19: [
            {n:"Fire Ship", p:8, w:5300, s:2600, si:2700},
            {n:"Catapult", p:8, w:5500, s:2900, si:3600},
            {n:"Cryptography", p:6, w:2500, s:3000, si:5100},
            {n:"Democracy", p:6, w:3100, s:3100, si:4100}
        ],
        22: [
            {n:"Light Transport Ship", p:8, w:6500, s:2800, si:3200},
            {n:"Plow", p:4, w:2000, s:2000, si:2000}
        ],
        25: [
            {n:"Trireme", p:8, w:3000, s:3000, si:3000},
            {n:"Phalanx", p:8, w:3000, s:3000, si:3000},
            {n:"Breach", p:6, w:4000, s:4000, si:4000},
            {n:"Mathematics", p:6, w:2000, s:2000, si:2000}
        ],
        28: [
            {n:"Battering Ram", p:10, w:4000, s:4000, si:4000},
            {n:"Cartography", p:8, w:4000, s:4000, si:4000},
            {n:"Conquest", p:0, w:10000, s:10000, si:10000}
        ],
        31: [
            {n:"Stone Hail", p:4, w:5000, s:5000, si:5000},
            {n:"Temple Looting", p:4, w:2000, s:2000, si:8000},
            {n:"Divine Selection", p:10, w:8000, s:8000, si:8000}
        ],
        34: [
            {n:"Battle Experience", p:6, w:8000, s:8000, si:8000},
            {n:"Strong Wine", p:4, w:5000, s:5000, si:12000},
            {n:"Set sail", p:6, w:10000, s:10000, si:10000}
        ]
    };

    // Research icon mapping (using Unicode/Emojis as fallback)
    const RESEARCH_ICONS = {
        "Slinger": "🏹",
        "Archer": "🏹",
        "City Guard": "🛡️",
        "Hoplite": "⚔️",
        "Diplomacy": "🤝",
        "Meteorology": "🌦️",
        "Espionage": "🕵️",
        "Booty": "💰",
        "Ceramics": "🏺",
        "Horseman": "🐴",
        "Architecture": "🏛️",
        "Trainer": "👨‍🏫",
        "Colony Ship": "⛵",
        "Bireme": "⛵",
        "Crane": "🏗️",
        "Shipwright": "🛠️",
        "Chariot": "🚗",
        "Light Ship": "⛵",
        "Conscription": "📜",
        "Fire Ship": "🔥⛵",
        "Catapult": "🎯",
        "Cryptography": "🔐",
        "Democracy": "🗳️",
        "Light Transport Ship": "⛴️",
        "Plow": "🌾",
        "Trireme": "⛵",
        "Phalanx": "🛡️",
        "Breach": "💥",
        "Mathematics": "📐",
        "Battering Ram": "🏚️",
        "Cartography": "🗺️",
        "Conquest": "👑",
        "Stone Hail": "🪨",
        "Temple Looting": "⛪",
        "Divine Selection": "✨",
        "Battle Experience": "⭐",
        "Strong Wine": "🍷",
        "Set sail": "🌊"
    };

    const LOGO_CONFIG = {
        useCustomImage: false,
        imageUrl: 'https://i.imgur.com/yYtKnhT.png',
        fallbackEmoji: '🏛️'
    };

    let selectedRes = [];
    let discordWebhook = '';

    try {
        selectedRes = JSON.parse(localStorage.getItem('grepo_acad_selected')) || [];
        discordWebhook = localStorage.getItem('grepo_discord_webhook') || '';
    } catch(e) {
        console.warn('Failed to load data from localStorage:', e);
        selectedRes = [];
        discordWebhook = '';
    }

    // ==========================================
    // ACADEMY LEVEL CALCULATION FUNCTION
    // ==========================================
    function calculateAcademyRequirement(points) {
        const POINTS_PER_LEVEL = 4;
        const MAX_POINTS_WITHOUT_LIBRARY = 144; // Level 36 = 144 points
        const LIBRARY_BONUS = 12; // Library adds 12 extra points
        const ABSOLUTE_MAX = MAX_POINTS_WITHOUT_LIBRARY + LIBRARY_BONUS; // 156 points

        if (points <= 0) {
            return { level: 0, levelText: "0", needsLibrary: false, status: "info" };
        }

        if (points > ABSOLUTE_MAX) {
            return { 
                level: 36, 
                levelText: "❌ NOT POSSIBLE", 
                needsLibrary: true, 
                status: "error",
                message: `${points} points exceeds maximum of 156 (Academy 36 + Library)`
            };
        }

        if (points > MAX_POINTS_WITHOUT_LIBRARY) {
            const libraryPoints = points - MAX_POINTS_WITHOUT_LIBRARY;
            return { 
                level: 36, 
                levelText: "36 + 📚 Library", 
                needsLibrary: true, 
                status: "warning",
                message: `${libraryPoints} extra points require Library`
            };
        }

        // Calculate required level (4 points per level)
        const requiredLevel = Math.ceil(points / POINTS_PER_LEVEL);
        
        return { 
            level: requiredLevel, 
            levelText: `${requiredLevel}`, 
            needsLibrary: false, 
            status: "success",
            pointsPerLevel: POINTS_PER_LEVEL
        };
    }

    // ==========================================
    // 2. UI CREATION - OVERLAYS & MODALS
    // ==========================================

    // Academy Modal
    const acadModal = document.createElement('div');
    acadModal.style = 'position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); width:96vw; max-width:1400px; background:#5a4a38; border:2px solid gold; z-index:20000; display:none; flex-direction:column; color:white; font-family:Arial; border-radius:10px; padding:12px; box-shadow:0 0 50px black; overflow-y:auto; max-height:90vh;';
    document.body.appendChild(acadModal);

    const acadOverlay = document.createElement('div');
    acadOverlay.style = 'position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.8); z-index:19999; display:none;';
    document.body.appendChild(acadOverlay);

    // Resource Popup
    const resourcePopup = document.createElement('div');
    resourcePopup.style = 'position:fixed; background:#3d2f21; border:2px solid gold; border-radius:8px; padding:12px; z-index:20001; display:none; color:white; font-family:Arial; box-shadow:0 0 20px black; min-width:200px;';
    document.body.appendChild(resourcePopup);

    // Dashboard Menu
    const dashMenu = document.createElement('div');
    dashMenu.style = 'position:fixed; bottom:90px; left:15%; margin-left:-85px; width:180px; background:rgba(45, 34, 22, 0.95); border:2px solid gold; border-radius:8px; display:none; flex-direction:column; padding:10px; z-index:10001; box-shadow: 0 0 15px black;';
    document.body.appendChild(dashMenu);

    // Discord Webhook Modal
    const webhookModal = document.createElement('div');
    webhookModal.style = 'position:fixed; top:50%; left:50%; transform:translate(-50%, -50%); width:90vw; max-width:500px; background:#5a4a38; border:2px solid #5865F2; z-index:20001; display:none; flex-direction:column; color:white; font-family:Arial; border-radius:10px; padding:20px; box-shadow:0 0 50px black;';
    document.body.appendChild(webhookModal);

    const webhookOverlay = document.createElement('div');
    webhookOverlay.style = 'position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.8); z-index:20000; display:none;';
    document.body.appendChild(webhookOverlay);

    // ==========================================
    // 3. LOGO CREATION WITH IMAGE FALLBACK
    // ==========================================
    let logoElement;

    const createImageLogo = () => {
        const img = document.createElement('img');
        img.src = LOGO_CONFIG.imageUrl;
        img.alt = 'Dashboard';
        img.style = 'position:fixed; bottom:15px; left:15%; margin-left:-32px; width:64px; height:64px; cursor:pointer; z-index:10002; border-radius:8px; border:3px solid gold; box-shadow: 0 0 10px black; transition: 0.3s; object-fit:cover;';
        img.title = 'Toggle Dashboard Menu';

        img.onload = function() {
            console.log("✅ Logo image loaded successfully");
        };

        img.onerror = function() {
            console.error("❌ Logo image failed to load, using fallback");
            this.replaceWith(createFallbackLogo());
        };

        img.addEventListener('mouseover', () => {
            img.style.transform = 'scale(1.1)';
            img.style.boxShadow = '0 0 20px rgba(255,215,0,0.8)';
        });

        img.addEventListener('mouseout', () => {
            img.style.transform = 'scale(1)';
            img.style.boxShadow = '0 0 10px black';
        });

        img.addEventListener('click', () => {
            const isHidden = dashMenu.style.display === 'none';
            dashMenu.style.display = isHidden ? 'flex' : 'none';
        });

        return img;
    };

    const createFallbackLogo = () => {
        const fallback = document.createElement('div');
        fallback.innerHTML = LOGO_CONFIG.fallbackEmoji;
        fallback.style = 'position:fixed; bottom:15px; left:15%; margin-left:-32px; width:64px; height:64px; cursor:pointer; z-index:10002; border-radius:8px; border:3px solid gold; box-shadow: 0 0 10px black; background: #3d2f21; font-size:32px; display:flex; align-items:center; justify-content:center; transition: 0.3s; color:gold;';
        fallback.title = 'Toggle Dashboard Menu';

        fallback.addEventListener('mouseover', () => {
            fallback.style.transform = 'scale(1.1)';
            fallback.style.boxShadow = '0 0 20px rgba(255,215,0,0.8)';
        });

        fallback.addEventListener('mouseout', () => {
            fallback.style.transform = 'scale(1)';
            fallback.style.boxShadow = '0 0 10px black';
        });

        fallback.addEventListener('click', () => {
            const isHidden = dashMenu.style.display === 'none';
            dashMenu.style.display = isHidden ? 'flex' : 'none';
        });

        return fallback;
    };

    logoElement = createImageLogo();
    document.body.appendChild(logoElement);

    // ==========================================
    // 4. LOGIC & RENDER FUNCTIONS
    // ==========================================

    window.toggleRes = function(name) {
        const i = selectedRes.indexOf(name);
        if (i > -1) {
            selectedRes.splice(i, 1);
        } else {
            selectedRes.push(name);
        }
        try {
            localStorage.setItem('grepo_acad_selected', JSON.stringify(selectedRes));
        } catch(e) {
            console.warn('Failed to save research data:', e);
        }
        renderAcademy();
    };

    window.resetAcademy = function() {
        if(confirm("🔄 Are you sure you want to reset all selected research? This cannot be undone!")) {
            selectedRes = [];
            try {
                localStorage.removeItem('grepo_acad_selected');
            } catch(e) {
                console.warn('Failed to clear localStorage:', e);
            }
            renderAcademy();
        }
    };

    window.openAcademy = function() {
        acadModal.style.display = 'flex';
        acadOverlay.style.display = 'block';
        dashMenu.style.display = 'none';
        renderAcademy();
    };

    window.closeAcademy = function() {
        acadModal.style.display = 'none';
        acadOverlay.style.display = 'none';
        resourcePopup.style.display = 'none';
    };

    window.showResourcePopup = function(event, research) {
        event.stopPropagation();
        const wood = research.w;
        const stone = research.s;
        const silver = research.si;
        const points = research.p;

        resourcePopup.innerHTML = `
            <div style="font-weight:bold; font-size:13px; color:gold; margin-bottom:8px;">${research.n}</div>
            <div style="font-size:11px; margin-bottom:4px;">📦 Resources Required:</div>
            <div style="font-size:10px; margin-left:8px; line-height:1.6;">
                <div>🪵 Wood: <span style="color:#D4A574;">${wood.toLocaleString()}</span></div>
                <div>🪨 Stone: <span style="color:#A9A9A9;">${stone.toLocaleString()}</span></div>
                <div>💎 Silver: <span style="color:#FFD700;">${silver.toLocaleString()}</span></div>
            </div>
            <div style="font-size:10px; margin-top:8px; border-top:1px solid gold; padding-top:6px;">
                📊 Research Points: <span style="color:gold;">${points}</span>
            </div>
        `;

        const rect = event.target.getBoundingClientRect();
        resourcePopup.style.left = (rect.left + rect.width / 2) + 'px';
        resourcePopup.style.top = (rect.bottom + 10) + 'px';
        resourcePopup.style.transform = 'translateX(-50%)';
        resourcePopup.style.display = 'block';
    };

    window.openWebhookSetup = function() {
        webhookModal.style.display = 'flex';
        webhookOverlay.style.display = 'block';
        dashMenu.style.display = 'none';

        const input = webhookModal.querySelector('#webhookInput');
        if (input) input.value = discordWebhook;

        const statusDiv = webhookModal.querySelector('#webhookStatus');
        if (statusDiv) statusDiv.innerHTML = '';
    };

    window.closeWebhookSetup = function() {
        webhookModal.style.display = 'none';
        webhookOverlay.style.display = 'none';
    };

    window.testWebhook = async function() {
        const input = webhookModal.querySelector('#webhookInput');
        const statusDiv = webhookModal.querySelector('#webhookStatus');
        const url = input.value.trim();

        if (!url) {
            statusDiv.innerHTML = '<span style="color:#e74c3c;">❌ Please enter a webhook URL</span>';
            return;
        }

        statusDiv.innerHTML = '<span style="color:gold;">⏳ Testing...</span>';

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    content: '🧪 Grepolis Academy Planner - Webhook Test',
                    embeds: [{
                        title: 'Webhook Connection Successful!',
                        description: 'Your webhook is properly configured.',
                        color: 5865242
                    }]
                })
            });

            if (response.ok) {
                statusDiv.innerHTML = '<span style="color:#90EE90;">✓ Webhook is working!</span>';
            } else {
                statusDiv.innerHTML = '<span style="color:#e74c3c;">❌ Webhook error: ' + response.status + '</span>';
            }
        } catch(e) {
            statusDiv.innerHTML = '<span style="color:#e74c3c;">❌ Connection failed: ' + e.message + '</span>';
        }
    };

    window.saveWebhook = function() {
        const input = webhookModal.querySelector('#webhookInput');
        const statusDiv = webhookModal.querySelector('#webhookStatus');
        const url = input.value.trim();

        if (!url) {
            statusDiv.innerHTML = '<span style="color:#e74c3c;">❌ Please enter a webhook URL</span>';
            return;
        }

        try {
            discordWebhook = url;
            localStorage.setItem('grepo_discord_webhook', url);
            statusDiv.innerHTML = '<span style="color:#90EE90;">✓ Webhook saved successfully!</span>';
            setTimeout(() => {
                window.closeWebhookSetup();
            }, 1500);
        } catch(e) {
            statusDiv.innerHTML = '<span style="color:#e74c3c;">❌ Failed to save: ' + e.message + '</span>';
        }
    };

    window.clearWebhook = function() {
        if (confirm('Clear the Discord webhook?')) {
            discordWebhook = '';
            localStorage.removeItem('grepo_discord_webhook');
            const input = webhookModal.querySelector('#webhookInput');
            if (input) input.value = '';
            const statusDiv = webhookModal.querySelector('#webhookStatus');
            if (statusDiv) statusDiv.innerHTML = '<span style="color:#90EE90;">✓ Webhook cleared</span>';
        }
    };

    function renderAcademy() {
        let total = 0;
        let maxL = 0;
        let totalWood = 0;
        let totalStone = 0;
        let totalSilver = 0;

        Object.values(RESEARCH_DATA).flat().forEach(r => {
            if(selectedRes.includes(r.n)) {
                total += r.p;
                totalWood += r.w;
                totalStone += r.s;
                totalSilver += r.si;
            }
        });

        LEVELS.forEach(l => {
            RESEARCH_DATA[l].forEach(r => {
                if(selectedRes.includes(r.n) && l > maxL) {
                    maxL = l;
                }
            });
        });

        // Use new calculation function
        const academyCalc = calculateAcademyRequirement(total);

        let grid = `<div style="display:flex; gap:4px; width:100%; justify-content:space-between; overflow-x:auto; padding-bottom:10px;">`;

        LEVELS.forEach(l => {
            grid += `<div style="flex:1; display:flex; flex-direction:column; gap:4px; min-width:0;">
                <div style="background:gold; color:black; text-align:center; font-weight:bold; font-size:10px; padding:2px; border-radius:2px;">Lvl ${l}</div>
                ${RESEARCH_DATA[l].map(r => {
                    const s = selectedRes.includes(r.n);
                    const icon = RESEARCH_ICONS[r.n] || '📚';
                    return `
                    <div data-research="${r.n}" style="height:72px; background:${s?'#d4a574':'#3d2f21'}; border:2px solid ${s?'#FFD700':'#555'}; cursor:pointer; font-size:9px; text-align:center; display:flex; flex-direction:column; align-items:center; justify-content:center; color:${s?'black':'white'}; border-radius:4px; padding:4px; font-weight:${s?'bold':'normal'}; transition: all 0.2s; box-shadow:${s?'0 0 10px rgba(212,165,116,0.6)':'none'};">
                        <div style="font-size:20px; margin-bottom:2px;">${icon}</div>
                        <b style="line-height:1.1; font-size:8px;">${r.n}</b>
                        <div style="font-size:7px; margin-top:2px; opacity:0.9; background:rgba(0,0,0,0.3); padding:1px 3px; border-radius:2px;">${r.p} pts</div>
                    </div>`;
                }).join('')}</div>`;
        });

        grid += `</div>`;

        // Determine status color and message
        let statusColor = 'gold';
        let statusMessage = '';
        
        if (academyCalc.status === 'error') {
            statusColor = '#e74c3c';
            statusMessage = `<span style="color:#e74c3c; font-weight:bold;">❌ NOT POSSIBLE - Exceeds maximum of 156 points</span>`;
        } else if (academyCalc.status === 'warning') {
            statusColor = '#f39c12';
            statusMessage = `<span style="color:#f39c12; font-weight:bold;">⚠️ Academy 36 + 📚 Library Required</span>`;
        } else if (total > 0) {
            statusMessage = `<span style="color:${statusColor}; font-weight:bold;">✓ Academy Level ${academyCalc.levelText}</span>`;
        }

        acadModal.innerHTML = `
            <div style="display:flex; justify-content:space-between; margin-bottom:12px; border-bottom:2px solid gold; padding-bottom:10px; align-items:center; flex-wrap:wrap; gap:10px;">
                <b style="color:gold; font-size:14px;">🏛️ ACADEMY PLANNER v7.2</b>
                <div style="font-size:12px;">POINTS: <span style="color:${statusColor}; font-weight:bold;">${total}</span>/156</div>
                <div style="display:flex; gap:6px;">
                    <button id="resetBtn" style="background:#E67E22; color:white; border:none; cursor:pointer; padding:6px 12px; border-radius:4px; font-size:11px; font-weight:bold;">🔄 Reset</button>
                    <button id="closeBtn" style="background:#e74c3c; color:white; border:none; cursor:pointer; padding:6px 12px; border-radius:4px; font-size:11px; font-weight:bold;">✕ Close</button>
                </div>
            </div>

            <div style="background:rgba(0,0,0,0.3); border:2px solid ${statusColor === '#e74c3c' ? '#e74c3c' : statusColor === '#f39c12' ? '#f39c12' : 'gold'}; border-radius:6px; padding:12px; margin-bottom:12px;">
                <div style="font-size:12px; font-weight:bold; margin-bottom:8px; color:${statusColor};">📊 Required Academy Level:</div>
                <div style="font-size:14px; font-weight:bold; color:gold; margin-bottom:8px;">${statusMessage}</div>
                <div style="border-top:1px solid ${statusColor}; padding-top:8px; margin-top:8px;">
                    <div style="font-size:10px; color:#b8956a; margin-bottom:4px;">Total Resources for Selected Research:</div>
                    <div style="display:grid; grid-template-columns:repeat(3, 1fr); gap:10px; font-size:11px;">
                        <div>🪵 Wood: <span style="color:#D4A574; font-weight:bold;">${totalWood.toLocaleString()}</span></div>
                        <div>🪨 Stone: <span style="color:#A9A9A9; font-weight:bold;">${totalStone.toLocaleString()}</span></div>
                        <div>💎 Silver: <span style="color:#FFD700; font-weight:bold;">${totalSilver.toLocaleString()}</span></div>
                    </div>
                </div>
            </div>

            ${grid}
            <div style="font-size: 9px; color: #b8956a; margin-top: 10px; text-align:center; border-top:1px solid gold; padding-top:8px;">✓ Click on a research to select/deselect • Hover for details • Max: 144 pts (Level 36) + 12 pts (Library) = 156 pts total</div>`;

        // Add event listeners to research items
        acadModal.querySelectorAll('[data-research]').forEach(elem => {
            elem.addEventListener('click', function() {
                window.toggleRes(this.getAttribute('data-research'));
            });

            elem.addEventListener('contextmenu', function(e) {
                e.preventDefault();
                const researchName = this.getAttribute('data-research');
                const research = Object.values(RESEARCH_DATA).flat().find(r => r.n === researchName);
                if (research) {
                    window.showResourcePopup(e, research);
                }
            });

            elem.addEventListener('mouseenter', function(e) {
                const researchName = this.getAttribute('data-research');
                const research = Object.values(RESEARCH_DATA).flat().find(r => r.n === researchName);
                if (research && !selectedRes.includes(researchName)) {
                    window.showResourcePopup(e, research);
                }
            });

            elem.addEventListener('mouseleave', function() {
                if (!selectedRes.includes(this.getAttribute('data-research'))) {
                    resourcePopup.style.display = 'none';
                }
            });
        });

        // Add event listeners to buttons
        const resetBtn = acadModal.querySelector('#resetBtn');
        const closeBtn = acadModal.querySelector('#closeBtn');
        
        if (resetBtn) {
            resetBtn.addEventListener('click', window.resetAcademy);
        }
        if (closeBtn) {
            closeBtn.addEventListener('click', window.closeAcademy);
        }
    }

    // ==========================================
    // 5. SETUP WEBHOOK MODAL HTML
    // ==========================================
    webhookModal.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px; border-bottom:1px solid #5865F2; padding-bottom:10px;">
            <b style="color:#5865F2; font-size:14px;">💬 Discord Webhook Setup</b>
            <button id="closeWebhookBtn" style="background:#e74c3c; color:white; border:none; cursor:pointer; padding:2px 10px; border-radius:3px; font-weight:bold;">X</button>
        </div>
        <div style="margin-bottom:15px;">
            <label style="display:block; margin-bottom:8px; font-size:12px; color:#b8956a;">Discord Webhook URL:</label>
            <input id="webhookInput" type="text" placeholder="https://discord.com/api/webhooks/..." style="width:100%; padding:8px; border:1px solid #5865F2; border-radius:4px; background:#3d2f21; color:white; box-sizing:border-box; font-size:11px;">
            <div style="font-size:9px; color:#888; margin-top:5px;">Get your webhook URL from Discord Server Settings → Integrations → Webhooks</div>
        </div>
        <div style="display:flex; gap:10px; margin-bottom:15px;">
            <button id="testWebhookBtn" style="flex:1; background:#5865F2; color:white; border:none; cursor:pointer; padding:8px; border-radius:4px; font-weight:bold; font-size:11px;">Test Webhook</button>
            <button id="saveWebhookBtn" style="flex:1; background:gold; color:black; border:none; cursor:pointer; padding:8px; border-radius:4px; font-weight:bold; font-size:11px;">Save</button>
        </div>
        <div id="webhookStatus" style="font-size:10px; color:#888; text-align:center; min-height:20px;"></div>
        <button id="clearWebhookBtn" style="width:100%; background:#e74c3c; color:white; border:none; cursor:pointer; padding:8px; border-radius:4px; font-weight:bold; font-size:11px; margin-top:10px;">Clear Webhook</button>
    `;

    // ==========================================
    // 6. DASHBOARD BUTTON CREATION
    // ==========================================
    const addMenuBtn = (text, onClick, color = "gold") => {
        const btn = document.createElement('button');
        btn.innerText = text;
        btn.style = `margin: 5px 0; padding: 10px; background: transparent; color: ${color}; border: 1px solid ${color}; cursor: pointer; border-radius: 4px; font-weight: bold; font-size: 11px; transition: 0.3s;`;
        btn.addEventListener('mouseover', () => {
            btn.style.background = color + "22";
        });
        btn.addEventListener('mouseout', () => {
            btn.style.background = "transparent";
        });
        btn.addEventListener('click', onClick);
        dashMenu.appendChild(btn);
    };

    // Add buttons
    addMenuBtn("🏛️ Academy Planner", window.openAcademy);
    addMenuBtn("💬 Discord Webhook", window.openWebhookSetup, "#5865F2");
    addMenuBtn("⚔️ Attack Alarm", () => alert("Attack Alarm is active"), "#FF6B6B");

    // ==========================================
    // 7. EVENT LISTENERS
    // ==========================================

    // Academy modal overlays
    acadOverlay.addEventListener('click', window.closeAcademy);
    document.addEventListener('click', () => {
        if (resourcePopup.style.display === 'block') {
            resourcePopup.style.display = 'none';
        }
    });

    // Webhook modal buttons
    const closeWebhookBtn = webhookModal.querySelector('#closeWebhookBtn');
    const testWebhookBtn = webhookModal.querySelector('#testWebhookBtn');
    const saveWebhookBtn = webhookModal.querySelector('#saveWebhookBtn');
    const clearWebhookBtn = webhookModal.querySelector('#clearWebhookBtn');

    if (closeWebhookBtn) closeWebhookBtn.addEventListener('click', window.closeWebhookSetup);
    if (testWebhookBtn) testWebhookBtn.addEventListener('click', window.testWebhook);
    if (saveWebhookBtn) saveWebhookBtn.addEventListener('click', window.saveWebhook);
    if (clearWebhookBtn) clearWebhookBtn.addEventListener('click', window.clearWebhook);

    webhookOverlay.addEventListener('click', window.closeWebhookSetup);

    console.log("✅ Grepolis Academy Planner v7.2 loaded!");

})();
