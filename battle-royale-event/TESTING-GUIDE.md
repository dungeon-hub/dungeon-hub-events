# 🧪 Quest Master Testing Guide

## 🏃 **Quick Local Testing Setup**

### **Option 1: Use the Built-in Test Server**
The plugin includes a PaperMC test runner. You can start a test server directly:

```bash
cd /Users/nirbhay_garg/Desktop/dungeon-hub-events-main
./gradlew :battle-royale-event:runServer
```

This will:
- Download and start a PaperMC 1.20.6 server
- Automatically install your plugin
- Open on localhost:25565

### **Option 2: Manual Server Setup**

1. **Download PaperMC 1.20.6**:
   ```bash
   mkdir ~/minecraft-test-server
   cd ~/minecraft-test-server
   wget https://api.papermc.io/v2/projects/paper/versions/1.20.6/builds/147/downloads/paper-1.20.6-147.jar
   ```

2. **Create server structure**:
   ```bash
   mkdir plugins
   cp /Users/nirbhay_garg/Desktop/dungeon-hub-events-main/battle-royale-event/build/libs/battle-royale-event-1.0-SNAPSHOT-all.jar plugins/
   ```

3. **Start server**:
   ```bash
   java -Xmx2G -Xms1G -jar paper-1.20.6-147.jar --nogui
   ```

## 🎮 **Testing Checklist**

### **1. Plugin Loading**
✅ Check console for: `"Dungeon Hub Battle Royale Plugin enabled with Quest GUI system!"`

### **2. Configuration Generation**
Check that these files are created:
- `plugins/battle-royale-event/questmasters.yml`
- `plugins/battle-royale-event/quests.yml`

### **3. Quest Master Commands**
Test admin commands (as OP):
```
/op YourUsername
/questmaster list
/questmaster spawn temple_zeroth
/questmaster info temple_zeroth
```

### **4. Quest Master NPCs**
- Use `/questmaster list` to see Quest Masters
- Use `/questmaster tp temple_zeroth` to teleport to one
- **Right-click the NPC** to open quest GUI

### **5. Quest GUI Testing**
- ✅ GUI opens with 5 quests
- ✅ Quest status shows correctly (Available/Active/Completed)
- ✅ Click quest to accept it
- ✅ Quest progress tracking works
- ✅ Rewards are given on completion

### **6. Quest Progress**
Test quest management:
```
/questmaster quest list
/questmaster quest complete zeroth_1
/questmaster gui temple_zeroth
```

## 🐛 **Troubleshooting**

### **Plugin Won't Load**
- Check Java version (needs Java 17+)
- Verify PaperMC version (1.20.6)
- Check console for error messages

### **Commands Don't Work**
- Make sure you're OP: `/op YourUsername`
- Check permission: `dungeonhub.questmaster.admin`

### **Quest Masters Don't Spawn**
- Check world name in config matches your world
- Use `/questmaster spawn <id>` to manually spawn
- Verify coordinates are valid

### **GUI Won't Open**
- Right-click the ArmorStand (invisible NPC)
- Check console for errors
- Try `/questmaster gui temple_zeroth` command

## 📊 **Expected Behavior**

### **On First Start**
1. Plugin loads successfully
2. Creates default config files
3. Spawns 5 Quest Master NPCs at temple locations
4. Console shows: "Loaded 5 Quest Masters"

### **Player Interaction**
1. Right-click Quest Master → Opens GUI
2. GUI shows 5 themed quests
3. Click available quest → Accepts with sound
4. Quest shows in progress with objectives
5. Complete quest → Rewards given

### **Admin Testing**
1. All `/questmaster` commands work
2. Tab completion functions
3. Reload command refreshes config
4. Spawn/remove commands work instantly

## 🎯 **Test Scenarios**

### **Basic Functionality**
1. ✅ Start server → Plugin loads
2. ✅ Right-click NPC → GUI opens
3. ✅ Click quest → Quest accepted
4. ✅ Complete quest → Rewards given

### **Quest Progression**
1. ✅ Accept multiple quests
2. ✅ Track progress in GUI
3. ✅ Complete prerequisite quest
4. ✅ Unlock master quest (quest 5)

### **Admin Management**
1. ✅ List all Quest Masters
2. ✅ Teleport to NPCs
3. ✅ Spawn/remove NPCs
4. ✅ Force complete quests

### **Configuration**
1. ✅ Edit quest configs
2. ✅ Reload plugin
3. ✅ Changes apply immediately
4. ✅ Custom quests work

## 🎮 **Demo Commands**

For quick testing, run these commands in order:

```bash
# 1. Check plugin status
/questmaster list

# 2. Teleport to first temple
/questmaster tp temple_zeroth

# 3. Open quest GUI
/questmaster gui temple_zeroth

# 4. Check your quest progress
/questmaster quest list

# 5. Force complete a quest (testing)
/questmaster quest complete zeroth_1

# 6. Check updated progress
/questmaster gui temple_zeroth
```

## ✅ **Success Indicators**

Your system is working if:
- ✅ Plugin loads without errors
- ✅ 5 Quest Masters spawn automatically
- ✅ Right-clicking NPCs opens quest GUI
- ✅ GUI shows 5 themed quests per temple
- ✅ Quest acceptance works with sound effects
- ✅ Quest progress tracks correctly
- ✅ Rewards are given on completion
- ✅ Admin commands function properly
