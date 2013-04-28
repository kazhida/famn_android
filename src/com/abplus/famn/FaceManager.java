package com.abplus.famn;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2013 ABplus Inc. kazhida
 * All rights reserved.
 * Author:  kazhida
 * Created: 2013/04/28 12:31
 */
public class FaceManager {

    class FaceItem {
        int id;
        int index;
        private FaceItem(int index, int id) {
            this.id = id;
            this.index = index;
        }
        int getIndex() {
            return index;
        }
    }

    Map<String, FaceItem[]> faces = new HashMap<String, FaceItem[]>();
    FaceItem[] current = null;

    private FaceManager() {
        faces.put("black", new FaceItem[] {
                new FaceItem(1, R.drawable.face_black_1),
                new FaceItem(2, R.drawable.face_black_2),
                new FaceItem(3, R.drawable.face_black_3),
                new FaceItem(4, R.drawable.face_black_4),
        });
        faces.put("blue", new FaceItem[] {
                new FaceItem(1, R.drawable.face_blue_1),
                new FaceItem(2, R.drawable.face_blue_2),
                new FaceItem(3, R.drawable.face_blue_3),
                new FaceItem(4, R.drawable.face_blue_4),
        });
        faces.put("brown", new FaceItem[] {
                new FaceItem(1, R.drawable.face_brown_1),
                new FaceItem(2, R.drawable.face_brown_2),
                new FaceItem(3, R.drawable.face_brown_3),
                new FaceItem(4, R.drawable.face_brown_4),
        });
        faces.put("gray", new FaceItem[] {
                new FaceItem(1, R.drawable.face_gray_1),
                new FaceItem(2, R.drawable.face_gray_2),
                new FaceItem(3, R.drawable.face_gray_3),
                new FaceItem(4, R.drawable.face_gray_4),
        });
        faces.put("green", new FaceItem[] {
                new FaceItem(1, R.drawable.face_green_1),
                new FaceItem(2, R.drawable.face_green_2),
                new FaceItem(3, R.drawable.face_green_3),
                new FaceItem(4, R.drawable.face_green_4),
        });
        faces.put("orange", new FaceItem[] {
                new FaceItem(1, R.drawable.face_orange_1),
                new FaceItem(2, R.drawable.face_orange_2),
                new FaceItem(3, R.drawable.face_orange_3),
                new FaceItem(4, R.drawable.face_orange_4),
        });
        faces.put("pink", new FaceItem[] {
                new FaceItem(1, R.drawable.face_pink_1),
                new FaceItem(2, R.drawable.face_pink_2),
                new FaceItem(3, R.drawable.face_pink_3),
                new FaceItem(4, R.drawable.face_pink_4),
        });
        faces.put("purple", new FaceItem[] {
                new FaceItem(1, R.drawable.face_purple_1),
                new FaceItem(2, R.drawable.face_purple_2),
                new FaceItem(3, R.drawable.face_purple_3),
                new FaceItem(4, R.drawable.face_purple_4),
        });
        faces.put("red", new FaceItem[] {
                new FaceItem(1, R.drawable.face_red_1),
                new FaceItem(2, R.drawable.face_red_2),
                new FaceItem(3, R.drawable.face_red_3),
                new FaceItem(4, R.drawable.face_red_4),
        });

    }

    private static FaceManager shared = null;
    public  static FaceManager sharedInstance() {
        if (shared == null) {
            shared = new FaceManager();
            shared.setFace("black");
        }
        return shared;
    }

    public void setFace(String face) {
        current = faces.get(face);
    }

    private class FaceAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private FaceAdapter(LayoutInflater inflater) {
            super();
            this.inflater = inflater;
        }


        @Override
        public int getCount() {
            if (current == null) {
                return 0;
            } else {
                return current.length;
            }
        }

        @Override
        public Object getItem(int position) {
            if (current == null) {
                return null;
            } else {
                return current[position];
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView result = (ImageView)convertView;

            if (result == null) {
                result = (ImageView)inflater.inflate(R.layout.face_item, null);
            }
            if (current != null) {
                FaceItem item = current[position];
                result.setTag(item);
                result.setImageResource(item.id);
            }

            return result;
        }
    }

    public BaseAdapter getAdapter(Activity activity) {
        return new FaceAdapter(activity.getLayoutInflater());
    }
}
