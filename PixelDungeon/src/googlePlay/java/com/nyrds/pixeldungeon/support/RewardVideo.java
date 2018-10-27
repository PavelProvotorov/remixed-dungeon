package com.nyrds.pixeldungeon.support;

import com.watabou.noosa.InterstitialPoint;

/**
 * Created by mike on 03.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class RewardVideo {
	static public void init() {
		if(!GoogleRewardVideoAds.isReady()){
			GoogleRewardVideoAds.initCinemaRewardVideo();
		}
	}

	static public boolean isReady() {
		return AppodealRewardVideo.isVideoReady() || GoogleRewardVideoAds.isReady();
	}

	public static void showCinemaRewardVideo(InterstitialPoint ret) {
		if(GoogleRewardVideoAds.isReady()) {
			GoogleRewardVideoAds.showCinemaRewardVideo(ret);
			return;
		}

		if (AppodealRewardVideo.isVideoReady()) {
			AppodealRewardVideo.showCinemaRewardVideo(ret);
			return;
		}

		ret.returnToWork(false);
	}
}
