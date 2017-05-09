/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.nyrds.android.util.Flavours;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.BuildConfig;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.support.Ads;
import com.nyrds.pixeldungeon.support.Iap;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.nyrds.pixeldungeon.support.RewardVideo;
import com.watabou.noosa.Game;
import com.watabou.noosa.SystemText;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.items.ItemSpritesDescription;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.scenes.WelcomeScene;
import com.watabou.pixeldungeon.ui.ModsButton;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.Locale;

import javax.microedition.khronos.opengles.GL10;

public class PixelDungeon extends Game {

	public static final double[] MOVE_TIMEOUTS = new double[]{250, 500, 1000, 2000, 5000, 10000, 30000, 60000, Double.POSITIVE_INFINITY };

	public PixelDungeon() {
		super(TitleScene.class);
		
		// remix 0.5
		com.watabou.utils.Bundle.addAlias(
				com.watabou.pixeldungeon.items.food.Ration.class,
				"com.watabou.pixeldungeon.items.food.Food");
		// remix 23.1.alpha
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.mobs.guts.SuspiciousRat.class,
				"com.nyrds.pixeldungeon.mobs.guts.Wererat");
		// remix 23.2.alpha
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.items.guts.weapon.melee.Claymore.class,
				"com.nyrds.pixeldungeon.items.guts.weapon.melee.BroadSword");
		// remix 24
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.items.accessories.Bowknot.class,
				"com.nyrds.pixeldungeon.items.accessories.BowTie");
		// remix 24
		com.watabou.utils.Bundle.addAlias(
				com.nyrds.pixeldungeon.items.accessories.Nightcap.class,
				"com.nyrds.pixeldungeon.items.accessories.SleepyHat");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RewardVideo.init();
		PlayGames.init(this);

		if(!isAlpha()) {
			PixelDungeon.realtime(false);
		}
		
		ModdingMode.selectMod(PixelDungeon.activeMod());
		PixelDungeon.activeMod(ModdingMode.activeMod());

		Iap.initIap(this);
		
		if(!Utils.canUseClassicFont(uiLanguage())) {
			PixelDungeon.classicFont(false);
		}
		
		ModdingMode.setClassicTextRenderingMode(PixelDungeon.classicFont());

		EventCollector.collectSessionData("font", String.valueOf(PixelDungeon.classicFont()));

		setSelectedLanguage();
		ItemSpritesDescription.readItemsDesc();

		updateImmersiveMode();

		DisplayMetrics metrics = new DisplayMetrics();
		instance().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		boolean landscape = metrics.widthPixels > metrics.heightPixels;

		if (Preferences.INSTANCE.getBoolean(Preferences.KEY_LANDSCAPE, false) != landscape) {
			landscape(!landscape);
		}

		Music.INSTANCE.enable(music());
		Sample.INSTANCE.enable(soundFx());

		if (PixelDungeon.version() != Game.versionCode) {
			switchScene(WelcomeScene.class);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		if(Preferences.INSTANCE.getBoolean(Preferences.KEY_USE_PLAY_GAMES,false)) {
			PlayGames.connect();
		}
	}

	public void setSelectedLanguage() {
		useLocale(uiLanguage());
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			updateImmersiveMode();
		}
	}



	public static String bundle2string(Bundle bundle) {
		if (bundle == null) {
			return null;
		}
		String string = "Bundle{";
		for (String key : bundle.keySet()) {
			string += " " + key + " => " + bundle.get(key) + ";";
		}
		string += " }Bundle";
		return string;
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		String extras = "";
		if(data!=null) {
			extras = bundle2string(data.getExtras());
		}

		GLog.i("onActivityResult(" + requestCode + "," + resultCode + "," + data +" "+extras);

		if(Iap.onActivityResult(requestCode, resultCode, data)) {
			return;
		}

		if(PlayGames.onActivityResult(requestCode, resultCode, data)) {
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public static void switchNoFade(Class<? extends PixelScene> c) {
		PixelScene.noFade = true;
		switchScene(c);
	}

	public static boolean canDonate() {
		return Flavours.haveDonations() && Iap.isReady() || BuildConfig.DEBUG;
	}
	
	/*
	 * ---> Preferences
	 */

	public static void landscape(boolean value) {
		Game.instance()
				.setRequestedOrientation(value ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
						: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Preferences.INSTANCE.put(Preferences.KEY_LANDSCAPE, value);
	}

	public static boolean landscape() {
		return width() > height();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);

		if (needSceneRestart && !(scene instanceof InterlevelScene)) {
			requestedReset = true;
			setNeedSceneRestart(false);
		}
	}

	@SuppressLint("NewApi")
	public static void updateImmersiveMode() {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			if (instance() != null) {
				instance().getWindow()
						.getDecorView()
						.setSystemUiVisibility(
								immersed() ? View.SYSTEM_UI_FLAG_LAYOUT_STABLE
										| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
										| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_FULLSCREEN
										| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
										: 0);
			}
		}
	}

	public static void zoom(double value) {
		Preferences.INSTANCE.put(Preferences.KEY_ZOOM, value);
	}

	public static double zoom() {
		return Preferences.INSTANCE.getDouble(Preferences.KEY_ZOOM, 0);
	}

	public static void music(boolean value) {
		Music.INSTANCE.enable(value);
		Preferences.INSTANCE.put(Preferences.KEY_MUSIC, value);
	}

	public static boolean music() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_MUSIC, true);
	}

	public static void soundFx(boolean value) {
		Sample.INSTANCE.enable(value);
		Preferences.INSTANCE.put(Preferences.KEY_SOUND_FX, value);
	}

	public static boolean soundFx() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_SOUND_FX, true);
	}

	public static void brightness(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_BRIGHTNESS, value);
		if (scene() instanceof GameScene) {
			((GameScene) scene()).brightness(value);
		}
	}

	public static boolean brightness() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_BRIGHTNESS,
				false);
	}

	private static void donated(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_DONATED, value);
	}

	public static int donated() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_DONATED, 0);
	}

	public static void lastClass(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_LAST_CLASS, value);
	}

	public static int lastClass() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_LAST_CLASS, 0);
	}

	public static void challenges(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_CHALLENGES, value);
	}

	public static int challenges() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_CHALLENGES, 0);
	}

	public static void intro(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_INTRO, value);
	}

	public static boolean intro() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_INTRO, true);
	}

	public static String uiLanguage() {
		String deviceLocale = Locale.getDefault().getLanguage();
		return Preferences.INSTANCE.getString(Preferences.KEY_LOCALE,
				deviceLocale);
	}

	public static void uiLanguage(String lang) {
		Preferences.INSTANCE.put(Preferences.KEY_LOCALE, lang);

		instance().doRestart();
	}

	public static void secondQuickslot(boolean checked) {
		Preferences.INSTANCE.put(Preferences.KEY_SECOND_QUICKSLOT, checked);
		if (scene() instanceof GameScene) {
			((GameScene) scene()).updateToolbar();
		}
	}

	public static boolean secondQuickslot() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_SECOND_QUICKSLOT, false);
	}

	public static void thirdQuickslot(boolean checked) {
		Preferences.INSTANCE.put(Preferences.KEY_THIRD_QUICKSLOT, checked);
		if (scene() instanceof GameScene) {
			((GameScene) scene()).updateToolbar();
		}
	}
	
	public static boolean thirdQuickslot() {
		return Preferences.INSTANCE.getBoolean(Preferences.KEY_THIRD_QUICKSLOT, false);
	}
	
	public static void version( int value)  {
        Preferences.INSTANCE.put( Preferences.KEY_VERSION, value );
    }

    public static int version() {
        return Preferences.INSTANCE.getInt( Preferences.KEY_VERSION, 0 );
    }

	public static void fontScale(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_FONT_SCALE, value);
		SystemText.updateFontScale();
	}

	public static int fontScale() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_FONT_SCALE, 0);
	}
	
	public static boolean classicFont() {
		boolean val = Preferences.INSTANCE.getBoolean(Preferences.KEY_CLASSIC_FONT, false);
		ModdingMode.setClassicTextRenderingMode(val);
		return val;
	}

	public static void classicFont(boolean value) {
		ModdingMode.setClassicTextRenderingMode(value);
		Preferences.INSTANCE.put(Preferences.KEY_CLASSIC_FONT, value);
	}

	public static void activeMod(String mod) {
		Preferences.INSTANCE.put(Preferences.KEY_ACTIVE_MOD, mod);
		ModdingMode.selectMod(PixelDungeon.activeMod());

		PixelDungeon.instance().setSelectedLanguage();

		EventCollector.collectSessionData("RPD_active_mod", ModdingMode.activeMod());
		EventCollector.collectSessionData("active_mod_version", Integer.toString(ModdingMode.activeModVersion()));
		ModsButton.modUpdated();
	}
	
	public static String activeMod() {
		return Preferences.INSTANCE.getString(Preferences.KEY_ACTIVE_MOD, ModdingMode.REMIXED);
	}

	public static boolean realtime() {
			return Preferences.INSTANCE.getBoolean(Preferences.KEY_REALTIME, false);
	}

	public static void realtime(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_REALTIME, value);
	}

	// *** IMMERSIVE MODE ****
	@SuppressLint("NewApi")
	public static void immerse(boolean value) {
		Preferences.INSTANCE.put(Preferences.KEY_IMMERSIVE, value);

		instance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateImmersiveMode();
				setNeedSceneRestart(true);
			}
		});
	}

	public static boolean immersed() {
		return Preferences.INSTANCE
				.getBoolean(Preferences.KEY_IMMERSIVE, true);
	}

	/*
	 * <--- Preferences
	 */

	/*
	 * <---Purchases
	 */
	static public void setDonationLevel(int level) {
		
		if(level > 0) {
			Ads.removeEasyModeBanner();
		}
		
		if (level < donated()) {
			return;
		}
		
		if (donated() == 0 && level != 0) {
			executeInGlThread(new Runnable() {
				
				@Override
				public void run() {
					Sample.INSTANCE.play(Assets.SND_GOLD);
					Badges.validateSupporter();
				}
			});
		}
		donated(level);
	}

	public static void setDifficulty(int _difficulty) {
		difficulty = _difficulty;

		if(donated() > 0) {
			Ads.removeEasyModeBanner();
			return;
		}

		if (PixelDungeon.donated() == 0) {
			if (getDifficulty() == 0) {
				Ads.displayEasyModeBanner();
			}

			if (getDifficulty() < 2) {
				Ads.initSaveAndLoadIntersitial();
			}

			if (getDifficulty() >= 2) {
				Ads.removeEasyModeBanner();
			}
		}
	}

	//--- Move timeouts
	public static int moveTimeout() {
		return Preferences.INSTANCE.getInt(Preferences.KEY_MOVE_TIMEOUT, Integer.MAX_VALUE);
	}

	public static void moveTimeout(int value) {
		Preferences.INSTANCE.put(Preferences.KEY_MOVE_TIMEOUT,value);
	}

	public static int limitTimeoutIndex(int value) {
		return 	Math.max(Math.min(value, MOVE_TIMEOUTS.length-1),0);
	}

	public static double getMoveTimeout() {
		return MOVE_TIMEOUTS[limitTimeoutIndex(moveTimeout())];
	}

}