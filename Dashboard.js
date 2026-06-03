// ==UserScript==
// @name         Grepolis Dashboard Complete v6.0
// @namespace    http://tampermonkey.net/
// @version      6.0
// @description  Dashboard: Discord + Academy + Helena + Vliegers
// @author       ChatGPT
// @match        *://*.grepolis.com/*
// @grant        GM_xmlhttpRequest
// @connect      discord.com
// @run-at       document-start
// ==/UserScript==

(function() {
    'use strict';
    console.log("🚀 Grepolis Dashboard v6.0 Starting...");

    // ==========================
    // IMMEDIATE STYLES
    // ==========================
    const style = document.createElement('style');
    style.textContent = `
        #grepolis-logo { position: fixed !important; bottom: 20px !important; left: 20px !important; width: 70px !important; height: 70px !important; z-index: 100000 !important; display: block !important; visibility: visible !important; }
        #grepolis-menu { position: fixed !important; bottom: 100px !important; left: 20px !important; z-index: 99999 !important; visibility: visible !important; }
    `;
    document.documentElement.appendChild(style);

    // ==========================
    // DATA
    // ==========================
    const ALERT_SOUND_URL = 'https://www.myinstants.com/media/sounds/bell.mp3';
    const LEVELS = [1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34];
    
    const RESEARCH_DATA = {
        1:  [{n:"Slinger", p:4, w:300, s:500, si:200},{n:"Archer", p:8, w:550, s:100, si:400},{n:"City Guard", p:4, w:400, s:300, si:300}],
        4:  [{n:"Hoplite", p:8, w:600, s:200, si:850},{n:"Diplomacy", p:4, w:100, s:400, si:200},{n:"Meteorology", p:4, w:2500, s:1700, si:6500}],
        7:  [{n:"Espionage", p:4, w:900, s:900, si:1100},{n:"Booty", p:8, w:1200, s:1200, si:1200},{n:"Ceramics", p:6, w:700, s:1500, si:900}],
        10: [{n:"Horseman", p:8, w:1400, s:700, si:1800},{n:"Architecture", p:6, w:1900, s:2100, si:1300},{n:"Trainer", p:4, w:800, s:1300, si:1600}],
        13: [{n:"Colony Ship", p:0, w:7500, s:7500, si:9500},{n:"Bireme", p:8, w:2800, s:1300, si:2200},{n:"Crane", p:4, w:3000, s:1800, si:1400},{n:"Shipwright", p:6, w:5000, s:2000, si:3900}],
        16: [{n:"Chariot", p:8, w:3700, s:1900, si:2800},{n:"Light Ship", p:8, w:4400, s:2000, si:2400},{n:"Conscription", p:4, w:3800, s:4200, si:6000}],
        19: [{n:"Fire Ship", p:8, w:5300, s:2600, si:2700},{n:"Catapult", p:8, w:5500, s:2900, si:3600},{n:"Cryptography", p:6, w:2500, s:3000, si:5100},{n:"Democracy", p:6, w:3100, s:3100, si:4100}],
        22: [{n:"Light Transport Ship", p:8, w:6500, s:2800, si:3200},{n:"Plow", p:4, w:2000, s:2000, si:2000}],
        25: [{n:"Trireme", p:8, w:3000, s:3000, si:3000},{n:"Phalanx", p:8, w:3000, s:3000, si:3000},{n:"Breach", p:6, w:4000, s:4000, si:4000},{n:"Mathematics", p:6, w:2000, s:2000, si:2000}],
        28: [{n:"Battering Ram", p:10, w:4000, s:4000, si:4000},{n:"Cartography", p:8, w:4000, s:4000, si:4000},{n:"Conquest", p:0, w:10000, s:10000, si:10000}],
        31: [{n:"Stone Hail", p:4, w:5000, s:5000, si:5000},{n:"Temple Looting", p:4, w:2000, s:2000, si:8000},{n:"Divine Selection", p:10, w:8000, s:8000, si:8000}],
        34: [{n:"Battle Experience", p:6, w:8000, s:8000, si:8000},{n:"Strong Wine", p:4, w:5000, s:5000, si:12000},{n:"Set sail", p:6, w:10000, s:10000, si:10000}]
    };

    const HELENA_DATA = [
        {level: 0, reduction: 0, time: 480, timeStr: "8:00", democratieTime: 528},
        {level: 1, reduction: 0.055, time: 454, timeStr: "7:34", democratieTime: 499.4},
        {level: 2, reduction: 0.06, time: 451.2, timeStr: "7:31", democratieTime: 496.32},
        {level: 3, reduction: 0.065, time: 448.8, timeStr: "7:29", democratieTime: 493.68},
        {level: 4, reduction: 0.07, time: 446.4, timeStr: "7:26", democratieTime: 491.04},
        {level: 5, reduction: 0.075, time: 444, timeStr: "7:24", democratieTime: 488.4},
        {level: 6, reduction: 0.08, time: 441.6, timeStr: "7:22", democratieTime: 485.76},
        {level: 7, reduction: 0.085, time: 439.2, timeStr: "7:19", democratieTime: 483.12},
        {level: 8, reduction: 0.09, time: 436.8, timeStr: "7:17", democratieTime: 480.48},
        {level: 9, reduction: 0.095, time: 434.4, timeStr: "7:14", democratieTime: 477.84},
        {level: 10, reduction: 0.1, time: 432, timeStr: "7:12", democratieTime: 475.2},
        {level: 11, reduction: 0.105, time: 429.6, timeStr: "7:09", democratieTime: 472.56},
        {level: 12, reduction: 0.11, time: 427.2, timeStr: "7:07", democratieTime: 469.92},
        {level: 13, reduction: 0.115, time: 424.8, timeStr: "7:04", democratieTime: 467.28},
        {level: 14, reduction: 0.12, time: 422.4, timeStr: "7:02", democratieTime: 464.64},
        {level: 15, reduction: 0.125, time: 420, timeStr: "7:00", democratieTime: 462},
        {level: 16, reduction: 0.13, time: 417.6, timeStr: "6:57", democratieTime: 459.36},
        {level: 17, reduction: 0.135, time: 415.2, timeStr: "6:55", democratieTime: 456.72},
        {level: 18, reduction: 0.14, time: 412.8, timeStr: "6:52", democratieTime: 454.08},
        {level: 19, reduction: 0.145, time: 410.4, timeStr: "6:50", democratieTime: 451.44},
        {level: 20, reduction: 0.15, time: 408, timeStr: "6:48", democratieTime: 448.8}
    ];

    const VLIEGERS_DATA = {
        attackers: [
            {name: "Harpy", type: "Afstand", defenders: "Boogschutters, Zwaarden, Strijdwagens, Cerberus", reason: "Hoge afstandsverdediging countert schietaanval"},
            {name: "Manticore", type: "Steek", defenders: "Hoplieten, Strijdwagens, Medusa", reason: "Steekverdediging is key"},
            {name: "Griffioen", type: "Afstand", defenders: "Boogschutters, Zwaarden, Strijdwagens, Boar", reason: "Massale afstandsverdediging nodig"},
            {name: "Pegasus", type: "Steek", defenders: "Hoplieten, Strijdwagens, Medusa", reason: "Lage steekaanval → makkelijk te counteren"}
        ],
        antiGeneral: ["60% Boogschutters", "30% Zwaardvechters", "10% Hoplieten", "Medusa / Cerberus"],
        antiManticore: ["70% Hoplieten", "30% Strijdwagens", "Medusa"],
        antiHarpyGriffoen: ["70% Boogschutters", "30% Zwaardvechters", "Cerberus / Boar"]
    };

    // ==========================
    // STATE
    // ==========================
    let alertSoundEnabled = true;
    let startTime = new Date();
    let attackCount = 0;
    let supportCount = 0;
    let selectedRes = [];

    try {
        selectedRes = JSON.parse(localStorage.getItem('grepo_acad_selected')) || [];
    } catch(e) {}

    // ==========================
    // DOM ELEMENTS
    // ==========================
    const logo = document.createElement('img');
    logo.src = 'https://i.imgur.com/yYtKnhT.png';
    logo.id = 'grepolis-logo';
    logo.style.cssText = `position: fixed !important; bottom: 20px !important; left: 20px !important; width: 50px !important; height: 50px !important; border: 3px solid gold !important; border-radius: 8px !important; cursor: pointer !important; z-index: 100000 !important; box-shadow: 0 0 12px rgba(0,0,0,0.4) !important; transition: 0.3s !important; object-fit: cover !important;`;

    const menu = document.createElement('div');
    menu.id = 'grepolis-menu';
    menu.style.cssText = `position: fixed !important; bottom: 80px !important; left: 20px !important; width: 280px !important; padding: 12px !important; background: rgba(20,20,20,0.95) !important; border: 2px solid gold !important; border-radius: 12px !important; box-shadow: 0 0 15px rgba(0,0,0,0.8) !important; color: white !important; font-family: Arial !important; display: none !important; z-index: 99999 !important; flex-direction: column !important; gap: 8px !important; max-height: 80vh !important; overflow-y: auto !important;`;

    const acadModal = document.createElement('div');
    acadModal.style.cssText = `position: fixed !important; top: 50% !important; left: 50% !important; transform: translate(-50%, -50%) !important; width: 96vw !important; max-width: 1400px !important; background: #5a4a38 !important; border: 2px solid gold !important; z-index: 20000 !important; display: none !important; flex-direction: column !important; color: white !important; font-family: Arial !important; border-radius: 10px !important; padding: 12px !important; box-shadow: 0 0 50px black !important; overflow-y: auto !important; max-height: 90vh !important;`;

    const acadOverlay = document.createElement('div');
    acadOverlay.style.cssText = `position: fixed !important; top: 0 !important; left: 0 !important; width: 100% !important; height: 100% !important; background: rgba(0,0,0,0.8) !important; z-index: 19999 !important; display: none !important;`;

    const helenaModal = document.createElement('div');
    helenaModal.style.cssText = `position: fixed !important; top: 50% !important; left: 50% !important; transform: translate(-50%, -50%) !important; width: 96vw !important; max-width: 1000px !important; background: #5a4a38 !important; border: 2px solid #9B59B6 !important; z-index: 20000 !important; display: none !important; flex-direction: column !important; color: white !important; font-family: Arial !important; border-radius: 10px !important; padding: 12px !important; box-shadow: 0 0 50px black !important; overflow-y: auto !important; max-height: 90vh !important;`;

    const helenaOverlay = document.createElement('div');
    helenaOverlay.style.cssText = `position: fixed !important; top: 0 !important; left: 0 !important; width: 100% !important; height: 100% !important; background: rgba(0,0,0,0.8) !important; z-index: 19999 !important; display: none !important;`;

    const vliegersModal = document.createElement('div');
    vliegersModal.style.cssText = `position: fixed !important; top: 50% !important; left: 50% !important; transform: translate(-50%, -50%) !important; width: 96vw !important; max-width: 1100px !important; background: #5a4a38 !important; border: 2px solid #3498DB !important; z-index: 20000 !important; display: none !important; flex-direction: column !important; color: white !important; font-family: Arial !important; border-radius: 10px !important; padding: 12px !important; box-shadow: 0 0 50px black !important; overflow-y: auto !important; max-height: 90vh !important;`;

    const vliegersOverlay = document.createElement('div');
    vliegersOverlay.style.cssText = `position: fixed !important; top: 0 !important; left: 0 !important; width: 100% !important; height: 100% !important; background: rgba(0,0,0,0.8) !important; z-index: 19999 !important; display: none !important;`;

    const discordModal = document.createElement('div');
    discordModal.style.cssText = `position: fixed !important; top: 50% !important; left: 50% !important; transform: translate(-50%, -50%) !important; width: 96vw !important; max-width: 500px !important; background: #5a4a38 !important; border: 2px solid #7289DA !important; z-index: 20000 !important; display: none !important; flex-direction: column !important; color: white !important; font-family: Arial !important; border-radius: 10px !important; padding: 12px !important; box-shadow: 0 0 50px black !important; overflow-y: auto !important;`;

    const discordOverlay = document.createElement('div');
    discordOverlay.style.cssText = `position: fixed !important; top: 0 !important; left: 0 !important; width: 100% !important; height: 100% !important; background: rgba(0,0,0,0.8) !important; z-index: 19999 !important; display: none !important;`;

    // ==========================
    // FUNCTIONS - DEFINED FIRST
    // ==========================

    function openHelena() {
        menu.style.display = 'none';
        helenaOverlay.style.display = 'block';
        helenaModal.style.display = 'flex';
        let html = `<div style="display:flex; justify-content:space-between; margin-bottom:12px; border-bottom:2px solid #9B59B6; padding-bottom:10px;">
            <b style="color:#9B59B6;">⏱️ HELENA CALCULATOR</b>
            <button id="closeHelenaBtn" style="background:#e74c3c; color:white; border:none; cursor:pointer; padding:6px 12px; border-radius:4px;">✕</button>
        </div><table style="width:100%; border-collapse:collapse; font-size:11px;">
        <tr style="background:rgba(0,0,0,0.3);"><th style="border:1px solid gold; padding:8px; color:gold;">Level</th><th style="border:1px solid gold; padding:8px;">Reduction</th><th style="border:1px solid gold; padding:8px;">Time</th><th style="border:1px solid gold; padding:8px;">hh:mm</th><th style="border:1px solid gold; padding:8px;">+ Democracy</th></tr>`;
        HELENA_DATA.forEach((row, idx) => {
            const bg = idx % 2 === 0 ? 'rgba(0,0,0,0.2)' : '';
            const demoMin = Math.round(row.democratieTime);
            const demoH = Math.floor(demoMin / 60);
            const demoM = demoMin % 60;
            html += `<tr style="background:${bg};"><td style="border:1px solid #555; padding:6px; color:gold;">${row.level}</td><td style="border:1px solid #555; padding:6px; color:#90EE90;">${(row.reduction*100).toFixed(1)}%</td><td style="border:1px solid #555; padding:6px;">${row.time}</td><td style="border:1px solid #555; padding:6px; color:#FFD700;">${row.timeStr}</td><td style="border:1px solid #555; padding:6px; color:#90EE90;">${demoH}:${String(demoM).padStart(2,'0')}</td></tr>`;
        });
        html += `</table>`;
        helenaModal.innerHTML = html;
        setTimeout(() => {
            const btn = document.getElementById('closeHelenaBtn');
            if(btn) btn.addEventListener('click', () => {
                helenaOverlay.style.display = 'none';
                helenaModal.style.display = 'none';
            });
        }, 10);
    }

    function openVliegers() {
        menu.style.display = 'none';
        vliegersOverlay.style.display = 'block';
        vliegersModal.style.display = 'flex';
        let html = `<div style="display:flex; justify-content:space-between; margin-bottom:12px; border-bottom:2px solid #3498DB; padding-bottom:10px;">
            <b style="color:#3498DB;">🦅 VLIEGERS GUIDE</b>
            <button id="closeVliegersBtn" style="background:#e74c3c; color:white; border:none; cursor:pointer; padding:6px 12px; border-radius:4px;">✕</button>
        </div><div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;"><div style="background:rgba(0,0,0,0.3); padding:12px;"><b style="color:gold;">⚔️ ATTACKERS</b>`;
        VLIEGERS_DATA.attackers.forEach(a => {
            html += `<div style="margin-top:8px; padding-top:8px; border-top:1px solid #555; font-size:10px;"><div style="color:gold; font-weight:bold;">${a.name} (${a.type})</div><div style="color:#90EE90;">▪ ${a.defenders}</div></div>`;
        });
        html += `</div><div style="background:rgba(0,0,0,0.3); padding:12px;"><b style="color:gold;">🛡️ DEFENSES</b>
            <div style="margin-top:8px; padding:8px; background:rgba(52,152,219,0.1); border-left:3px solid #3498DB; font-size:10px;"><div style="color:gold; font-weight:bold;">General:</div>`;
        VLIEGERS_DATA.antiGeneral.forEach(d => { html += `<div style="color:#b8956a;">✓ ${d}</div>`; });
        html += `</div><div style="margin-top:8px; padding:8px; background:rgba(155,89,182,0.1); border-left:3px solid #9B59B6; font-size:10px;"><div style="color:gold; font-weight:bold;">Anti-Manticore:</div>`;
        VLIEGERS_DATA.antiManticore.forEach(d => { html += `<div style="color:#b8956a;">✓ ${d}</div>`; });
        html += `</div><div style="margin-top:8px; padding:8px; background:rgba(52,152,219,0.1); border-left:3px solid #3498DB; font-size:10px;"><div style="color:gold; font-weight:bold;">Anti-Harpy/Griffioen:</div>`;
        VLIEGERS_DATA.antiHarpyGriffoen.forEach(d => { html += `<div style="color:#b8956a;">✓ ${d}</div>`; });
        html += `</div></div></div>`;
        vliegersModal.innerHTML = html;
        setTimeout(() => {
            const btn = document.getElementById('closeVliegersBtn');
            if(btn) btn.addEventListener('click', () => {
                vliegersOverlay.style.display = 'none';
                vliegersModal.style.display = 'none';
            });
        }, 10);
    }

    function openAcademy() {
        menu.style.display = 'none';
        acadOverlay.style.display = 'block';
        acadModal.style.display = 'flex';
        
        // Calculate total points
        let total = 0;
        let minLevel = 0;
        
        Object.values(RESEARCH_DATA).flat().forEach(r => { 
            if(selectedRes.includes(r.n)) {
                total += r.p;
                // Find the level this research is in
                for(let lvl of LEVELS) {
                    if(RESEARCH_DATA[lvl].some(x => x.n === r.n)) {
                        minLevel = Math.max(minLevel, lvl);
                    }
                }
            }
        });
        
        // Cap at 156 points max (36 levels + library)
        let exceeded = false;
        if(total > 156) {
            exceeded = true;
            total = 156;
        }
        
        // Calculate required level
        let levelText = "";
        if (total === 0) {
            levelText = "0";
        } else if (total <= 144) {
            // 4 points per level
            levelText = `${Math.max(minLevel, Math.ceil(total / 4))}`;
        } else {
            // 145-156 points requires level 36 + library
            levelText = `36 + 📚 Library`;
        }
        
        let warningHTML = exceeded ? `<div style="background:#e74c3c; border:2px solid #c0392b; padding:12px; margin-bottom:12px; border-radius:6px; color:white;">⚠️ <b>Warning:</b> Maximum 156 points allowed (Level 36 + Library). Excess points removed.</div>` : '';
        
        let grid = `<div style="display:flex; gap:4px; width:100%; overflow-x:auto; padding-bottom:10px;">`;
        LEVELS.forEach(l => {
            grid += `<div style="flex:1; display:flex; flex-direction:column; gap:4px;">
                <div style="background:gold; color:black; text-align:center; font-weight:bold; font-size:10px; padding:2px; border-radius:2px;">Lvl ${l}</div>`;
            RESEARCH_DATA[l].forEach(r => {
                const s = selectedRes.includes(r.n);
                grid += `<div class="acad-btn" data-name="${r.n}" style="height:62px; background:${s?'#d4a574':'#3d2f21'}; border:1px solid ${s?'gold':'#555'}; cursor:pointer; font-size:9px; text-align:center; display:flex; flex-direction:column; align-items:center; justify-content:center; color:${s?'black':'white'}; padding:2px; border-radius:3px;">
                    <b>${r.n}</b><div style="font-size:8px;">${r.p} pts</div>
                </div>`;
            });
            grid += `</div>`;
        });
        grid += `</div>`;
        acadModal.innerHTML = `<div style="display:flex; justify-content:space-between; margin-bottom:12px; border-bottom:2px solid gold; padding-bottom:10px;"><b style="color:gold;">🏛️ ACADEMY PLANNER</b><div style="font-size:12px;">POINTS: <span style="color:gold; font-weight:bold;" id="acadPoints">${total}</span>/156 | LEVEL: <span style="color:gold;" id="acadLevel">${levelText}</span></div><button id="closeAcadBtn" style="background:#e74c3c; color:white; border:none; cursor:pointer; padding:6px 12px; border-radius:4px;">✕</button></div>${warningHTML}${grid}`;
        
        setTimeout(() => {
            // Attach close button
            const closeBtn = document.getElementById('closeAcadBtn');
            if(closeBtn) closeBtn.addEventListener('click', () => {
                acadOverlay.style.display = 'none';
                acadModal.style.display = 'none';
            });
            
            // Attach research buttons
            const buttons = document.querySelectorAll('.acad-btn');
            buttons.forEach(btn => {
                btn.addEventListener('click', function() {
                    const name = this.getAttribute('data-name');
                    toggleRes(name);
                });
            });
        }, 10);
    }

    function openDiscord() {
        menu.style.display = 'none';
        discordOverlay.style.display = 'block';
        discordModal.style.display = 'flex';
        
        let webhookURL = localStorage.getItem('grepo_discord_webhook') || '';
        let playerName = localStorage.getItem('grepo_player_name') || '';
        
        discordModal.innerHTML = `
            <div style="display:flex; justify-content:space-between; margin-bottom:12px; border-bottom:2px solid #7289DA; padding-bottom:10px;">
                <b style="color:#7289DA;">🔔 DISCORD WEBHOOK CONFIG</b>
                <button id="closeDiscordBtn" style="background:#e74c3c; color:white; border:none; cursor:pointer; padding:6px 12px; border-radius:4px;">✕</button>
            </div>
            
            <div style="margin-bottom:12px;">
                <label style="color:gold; font-weight:bold; font-size:12px;">Player Name:</label>
                <input id="playerNameInput" type="text" placeholder="Your player name" style="width:100%; padding:8px; margin-top:4px; background:#3d2f21; border:1px solid #7289DA; color:white; border-radius:4px; box-sizing:border-box;" value="${playerName}">
            </div>
            
            <div style="margin-bottom:12px;">
                <label style="color:gold; font-weight:bold; font-size:12px;">Webhook URL:</label>
                <input id="webhookInput" type="text" placeholder="https://discord.com/api/webhooks/..." style="width:100%; padding:8px; margin-top:4px; background:#3d2f21; border:1px solid #7289DA; color:white; border-radius:4px; box-sizing:border-box;" value="${webhookURL}">
            </div>
            
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px; margin-bottom:12px;">
                <button id="saveDiscordBtn" style="padding:8px; background:#27AE60; color:white; border:none; cursor:pointer; font-weight:bold; border-radius:4px;">💾 Save</button>
                <button id="testDiscordBtn" style="padding:8px; background:#E67E22; color:white; border:none; cursor:pointer; font-weight:bold; border-radius:4px;">✓ Test</button>
            </div>
            
            <button id="removeDiscordBtn" style="width:100%; padding:8px; background:#e74c3c; color:white; border:none; cursor:pointer; font-weight:bold; border-radius:4px; margin-bottom:12px;">🗑️ Remove</button>
            
            <div style="background:rgba(114,137,218,0.1); padding:10px; border-left:3px solid #7289DA; border-radius:4px; font-size:10px; color:#b8956a;">
                <b style="color:#7289DA;">Test Message:</b>
                <div style="margin-top:8px; padding:8px; background:#3d2f21; border-radius:4px; white-space:pre-wrap; font-family:monospace;">🚨 <b>Grepolis Attack Alert</b>
<b>Player:</b> ${playerName || 'Your Name'}
<b>Title:</b> Je kolonisatieschip wordt aangevallen!
<b>Message:</b> Incoming attack detected
<b>Total attacks:</b> 1</div>
            </div>
        `;
        
        setTimeout(() => {
            const closeBtn = document.getElementById('closeDiscordBtn');
            const saveBtn = document.getElementById('saveDiscordBtn');
            const testBtn = document.getElementById('testDiscordBtn');
            const removeBtn = document.getElementById('removeDiscordBtn');
            const playerInput = document.getElementById('playerNameInput');
            const webhookInput = document.getElementById('webhookInput');
            
            if(closeBtn) closeBtn.addEventListener('click', () => {
                discordOverlay.style.display = 'none';
                discordModal.style.display = 'none';
            });
            
            if(saveBtn) saveBtn.addEventListener('click', () => {
                const playerName = playerInput.value.trim();
                const webhookURL = webhookInput.value.trim();
                
                if(!playerName) {
                    alert('Please enter your player name');
                    return;
                }
                if(!webhookURL) {
                    alert('Please enter a webhook URL');
                    return;
                }
                
                localStorage.setItem('grepo_discord_webhook', webhookURL);
                localStorage.setItem('grepo_player_name', playerName);
                alert('✓ Discord settings saved!');
            });
            
            if(testBtn) testBtn.addEventListener('click', () => {
                const playerName = playerInput.value.trim();
                const webhookURL = webhookInput.value.trim();
                
                if(!playerName) {
                    alert('Please enter your player name first');
                    return;
                }
                if(!webhookURL) {
                    alert('Please enter a webhook URL first');
                    return;
                }
                
                const payload = {
                    embeds: [{
                        title: '🚨 Grepolis Attack Alert',
                        color: 0xe74c3c,
                        fields: [
                            {name: 'Player', value: playerName},
                            {name: 'Title', value: 'Je kolonisatieschip wordt aangevallen!'},
                            {name: 'Message', value: 'Incoming attack detected'},
                            {name: 'Total attacks', value: '1'}
                        ],
                        footer: {text: 'Grepolis Dashboard'},
                        timestamp: new Date().toISOString()
                    }]
                };
                
                GM_xmlhttpRequest({
                    method: 'POST',
                    url: webhookURL,
                    headers: {'Content-Type': 'application/json'},
                    data: JSON.stringify(payload),
                    onload: (response) => {
                        if(response.status === 204 || response.status === 200) {
                            alert('✓ Test message sent to Discord!');
                        } else {
                            alert('✗ Failed to send message. Status: ' + response.status);
                        }
                    },
                    onerror: () => {
                        alert('✗ Error sending to Discord. Check webhook URL.');
                    }
                });
            });
            
            if(removeBtn) removeBtn.addEventListener('click', () => {
                if(confirm('Remove Discord settings?')) {
                    localStorage.removeItem('grepo_discord_webhook');
                    localStorage.removeItem('grepo_player_name');
                    playerInput.value = '';
                    webhookInput.value = '';
                    alert('✓ Discord settings removed');
                }
            });
        }, 10);
    }
    
    function closeDiscord() {
        discordOverlay.style.display = 'none';
        discordModal.style.display = 'none';
    }

    function toggleRes(name) {
        const i = selectedRes.indexOf(name);
        if (i > -1) selectedRes.splice(i, 1);
        else selectedRes.push(name);
        try { localStorage.setItem('grepo_acad_selected', JSON.stringify(selectedRes)); } catch(e) {}
        openAcademy();
    }

    // ==========================
    // APPEND AND SETUP
    // ==========================
    function setup() {
        const target = document.documentElement || document.body;
        if (!target) { setTimeout(setup, 50); return; }

        target.appendChild(logo);
        target.appendChild(menu);
        target.appendChild(acadOverlay);
        target.appendChild(acadModal);
        target.appendChild(helenaOverlay);
        target.appendChild(helenaModal);
        target.appendChild(vliegersOverlay);
        target.appendChild(vliegersModal);
        target.appendChild(discordOverlay);
        target.appendChild(discordModal);

        menu.innerHTML = `
            <div style="text-align:center; font-weight:bold; margin-bottom:8px; font-size:13px; color:gold; border-bottom:1px solid rgba(255,215,0,0.3); padding-bottom:8px;">⚙️ DASHBOARD</div>
            <div id="attackStatus" style="font-size:12px;">⚔️ Attacks: <b>0</b></div>
            <div id="supportStatus" style="font-size:12px;">🛡️ Support: <b>0</b></div>
            <div id="lastPing" style="font-size:11px;">🔔 Discord: <span style="color:gray;">None</span></div>
            <div id="uptime" style="font-size:11px;">⏱️ Uptime: <span id="upTimeValue">00:00:00</span></div>
            <div style="border-top:1px solid rgba(255,215,0,0.2); padding-top:8px; margin-top:8px; font-size:12px;">
                <label style="cursor:pointer;"><input type="checkbox" id="toggleSound" checked> Sound</label>
            </div>
            <button id="testAlarmBtn" style="width:100%; padding:8px; border:1px solid #E67E22; background:#E67E22; color:white; cursor:pointer; font-weight:bold; border-radius:4px; font-size:11px;">Test Alarm</button>
            <button id="discordBtn" style="width:100%; padding:8px; border:1px solid #7289DA; background:#7289DA; color:white; cursor:pointer; font-weight:bold; border-radius:4px; font-size:11px;">🔔 Discord</button>
            <button id="helenaBtn" style="width:100%; padding:8px; border:1px solid #9B59B6; background:#9B59B6; color:white; cursor:pointer; font-weight:bold; border-radius:4px; font-size:11px;">⏱️ Helena</button>
            <button id="vliegersBtn" style="width:100%; padding:8px; border:1px solid #3498DB; background:#3498DB; color:white; cursor:pointer; font-weight:bold; border-radius:4px; font-size:11px;">🦅 Vliegers</button>
            <button id="academyBtn" style="width:100%; padding:8px; border:1px solid #27AE60; background:#27AE60; color:white; cursor:pointer; font-weight:bold; border-radius:4px; font-size:11px;">🏛️ Academy</button>
        `;

        // NOW attach listeners
        document.getElementById('testAlarmBtn').addEventListener('click', () => {
            if(alertSoundEnabled) new Audio(ALERT_SOUND_URL).play();
            alert('Test Alarm!');
        });
        document.getElementById('discordBtn').addEventListener('click', openDiscord);
        document.getElementById('helenaBtn').addEventListener('click', openHelena);
        document.getElementById('vliegersBtn').addEventListener('click', openVliegers);
        document.getElementById('academyBtn').addEventListener('click', openAcademy);
        document.getElementById('toggleSound').addEventListener('change', function() { alertSoundEnabled = this.checked; });

        logo.addEventListener('click', () => {
            menu.style.display = menu.style.display === 'none' ? 'flex' : 'none';
        });
        logo.addEventListener('mouseover', () => { logo.style.transform = 'scale(1.15)'; logo.style.boxShadow = '0 0 30px rgba(255,215,0,0.9)'; });
        logo.addEventListener('mouseout', () => { logo.style.transform = 'scale(1)'; logo.style.boxShadow = '0 0 12px rgba(0,0,0,0.4)'; });

        acadOverlay.addEventListener('click', () => {
            acadOverlay.style.display = 'none';
            acadModal.style.display = 'none';
        });
        helenaOverlay.addEventListener('click', () => {
            helenaOverlay.style.display = 'none';
            helenaModal.style.display = 'none';
        });
        vliegersOverlay.addEventListener('click', () => {
            vliegersOverlay.style.display = 'none';
            vliegersModal.style.display = 'none';
        });
        discordOverlay.addEventListener('click', () => {
            discordOverlay.style.display = 'none';
            discordModal.style.display = 'none';
        });

        // Uptime
        setInterval(() => {
            const elapsed = new Date(new Date() - startTime);
            const hh = String(elapsed.getUTCHours()).padStart(2, '0');
            const mm = String(elapsed.getUTCMinutes()).padStart(2, '0');
            const ss = String(elapsed.getUTCSeconds()).padStart(2, '0');
            const el = document.getElementById('upTimeValue');
            if(el) el.textContent = `${hh}:${mm}:${ss}`;
        }, 1000);

        console.log("✅ Grepolis Dashboard v6.0 fully loaded!");
    }

    setup();

})();

