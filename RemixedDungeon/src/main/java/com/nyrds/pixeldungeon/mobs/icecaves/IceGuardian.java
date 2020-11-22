package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Amok;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.FrozenCarpaccio;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.utils.Random;

public class IceGuardian extends MultiKindMob {

	public IceGuardian() {
		hp(ht(70));
		exp = 5;
		baseDefenseSkill = 30;
		baseAttackSkill  = 31;

		kind = 1;


		baseSpeed = 0.7f;
		
		loot(new FrozenCarpaccio(), 0.2f);

		addImmunity( Paralysis.class );
		addImmunity( ToxicGas.class );
		addImmunity( Terror.class );
		addImmunity( Death.class );
		addImmunity( Amok.class );
		addImmunity( Blindness.class );
		addImmunity( Sleep.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 15 );
	}

	
	@Override
	public int dr() {
		return 14;
	}


	@Override
	public void die(NamedEntityKind cause) {
		super.die(cause);
		for (Mob mob : level().getCopyOfMobsArray()) {
			if (mob instanceof IceGuardianCore) {
				mob.damage(150,cause);
				if (mob.isAlive()){
					resurrect();
					resurrect();
				}
			}
		}
	}

}
