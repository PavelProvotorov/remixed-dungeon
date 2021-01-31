---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 1/26/21 10:02 PM
---

local RPD = require "scripts/lib/commonClasses"

local spell = require "scripts/lib/spell"

return spell.init{
    desc  = function ()
        return {
            image         = 2,
            imageFile     = "spellsIcons/naturegift.png",
            name          = "HideInGrassSpell_Name",
            info          = "HideInGrassSpell_Info",
            magicAffinity = "Elf",
            targetingType = "self",
            level         = 2,
            spellCost     = 5,
            cooldown      = 10,
            castTime      = 0.1
        }
    end,

    cast = function(self, spell, caster)
        RPD.topEffect(caster:getPos(),"hide_in_grass")
        local duration = caster:skillLevel() * 5

        local terrain = caster:level().map[caster:getPos()+1]

        if terrain == RPD.Terrain.GRASS or terrain == RPD.Terrain.HIGH_GRASS then
            RPD.affectBuff(caster,"Cloak", duration)
            return true
        end

        RPD.glogn("HideInGrassSpell_NeedGrassToHide")
        return false
    end}
