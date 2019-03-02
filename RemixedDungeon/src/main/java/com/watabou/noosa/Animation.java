package com.watabou.noosa;

import android.graphics.RectF;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by mike on 29.03.2016.
 * This file is part of Remixed Pixel Dungeon.
 */
public class Animation {

	public float   delay;
	public RectF[] frames;
	public boolean looped;

	//for auto conversion only
	public int[]   intFrames;

	public Animation(int fps, boolean looped) {
		this.delay = 1f / fps;
		this.looped = looped;
	}

	public Animation frames(RectF... frames) {
		this.frames = frames;
		return this;
	}

	public Animation frames(int shift, TextureFilm film, int... frames) {
		this.frames = new RectF[frames.length];
		for (int i = 0; i < frames.length; i++) {
			this.frames[i] = film.get(frames[i] + shift);
		}
		return this;
	}

	public void frames(TextureFilm film, int... frames) {
		this.frames = new RectF[frames.length];
		for (int i = 0; i < frames.length; i++) {
			this.frames[i] = film.get(frames[i]);
		}
		intFrames = frames;
	}

	public Animation clone() {
		Animation ret = new Animation(Math.round(1 / delay), looped).frames(frames);
		ret.intFrames = intFrames;
		return ret;

	}

	public void frames(TextureFilm film, List<Integer> frames, int shift) {
		this.frames = new RectF[frames.size()];
		for (int i = 0; i < frames.size(); i++) {
			this.frames[i] = film.get(frames.get(i) + shift);
		}
	}

	public JSONObject toJson() throws JSONException {
		JSONObject ret = new JSONObject();
		ret.put("fps",Math.round(1/delay));
		ret.put("looped", looped);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			ret.put("frames",new JSONArray(intFrames));
		}
		return ret;
	}

	public static class AnimationSeq {
		public int     fps;
		public int[]   frames;
		public boolean looped;
	}
}
