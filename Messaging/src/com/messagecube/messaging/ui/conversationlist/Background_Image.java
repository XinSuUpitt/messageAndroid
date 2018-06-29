package com.messagecube.messaging.ui.conversationlist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.messagecube.messaging.R;
import com.messagecube.messaging.ui.conversation.ConversationActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import static com.messagecube.messaging.ui.conversationlist.Background_Image.BACKGROUNDLIST;
import static com.messagecube.messaging.ui.conversationlist.Background_Image.BACKGROUNDLIST_FULL;

/**
 * Created by suxin on 9/30/17.
 */

public class Background_Image extends Activity{

    public static final String BACKGROUNDLIST = "background_list";
    public static final String BACKGROUNDLIST_FULL = "background_list_full";
    public BackGroundListViewAdapter adapter;
    public String messageId;
    private static int RESULT_LOAD_IMAGE = 1;
    private boolean shouldClose = true;
    private static final int PICK_FROM_GALLERY = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.background_image);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.add_floating);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(Background_Image.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Background_Image.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                }
                shouldClose = false;
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        messageId = getIntent().getStringExtra("messageIdForBackground");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.background_list_view);


        SharedPreferences sharedPreference = getSharedPreferences(BACKGROUNDLIST, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        ArrayList<String> set = gson.fromJson(sharedPreference.getString(BACKGROUNDLIST_FULL, ""), ArrayList.class);

        if (set == null) {
            set = new ArrayList<String>();
            set.add(""+R.drawable.background_default);
            set.add(""+R.drawable.background1);
            set.add(""+R.drawable.background2);
            set.add(""+R.drawable.background3);
            set.add(""+R.drawable.background4);
            set.add(""+R.drawable.background5);
            set.add(""+R.drawable.background6);
            set.add(""+R.drawable.background7);
            set.add(""+R.drawable.background8);
       //     set.add(""+R.drawable.background9);
       //     set.add(""+R.drawable.background10);
       //     set.add(""+R.drawable.background11);
        }

        String json1 = gson.toJson(set);
        sharedPreference.edit().putString(BACKGROUNDLIST_FULL, json1).apply();

        adapter = new BackGroundListViewAdapter(set, messageId, this);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new SpaceItemDecorationForGrid(getResources().getDimensionPixelOffset(R.dimen.space_medium)));
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shouldClose) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shouldClose) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();


            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);

            File img = new File(picturePath);
            long length = img.length();
            System.out.println("file length is: " + length/1024 + "KB");

            if (length/1024 >= 1000) {

                String filename=picturePath.substring(picturePath.lastIndexOf("/") + 1);

               File compressed_file = saveBitmapToFile(img);
               System.out.println("compressed file length: " + compressed_file.length());

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("backgrounds", Context.MODE_PRIVATE);
                System.out.println("directory for caching is: " + directory);

                File mypath = new File(directory, filename);
                System.out.println("compressed path is: " + mypath);

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    FileInputStream inputStream = new FileInputStream(compressed_file);

                    Bitmap compressed_bitmap = BitmapFactory.decodeStream(inputStream, null, null);
                    compressed_bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    picturePath = mypath.getAbsolutePath();
                } catch (Exception e) {
                    Log.e("SAVE_IMAGE", e.getMessage(), e);
                }

            }


            System.out.println("background list: " + BACKGROUNDLIST);
            SharedPreferences sharedPreference1 = getSharedPreferences(BACKGROUNDLIST, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            ArrayList<String> set = gson.fromJson(sharedPreference1.getString(BACKGROUNDLIST_FULL, ""), ArrayList.class);
            System.out.println("BACKGROUNDLIST_FULL set is: " + set);
            if (set == null) {
                set = new ArrayList<String>();
            }
            set.add(picturePath);
            String json1 = gson.toJson(set);
            sharedPreference1.edit().putString(BACKGROUNDLIST_FULL, json1).apply();


            System.out.println("save to local + " + picturePath);
            cursor.close();

            finish();
            startActivity(getIntent());


        }


    }

    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 40;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            System.out.println("image outWidth: " + o.outWidth + "image outHeight: " + o.outHeight);

            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }
}


class BackGroundListViewAdapter extends RecyclerView.Adapter<BackGroundViewHolder> {

    private ArrayList<String> set;
    private LayoutInflater inflater;
    private String messageId;

    BackGroundListViewAdapter(ArrayList<String> set, String messageId, Context context) {
        this.messageId = messageId;
        this.set = set;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public BackGroundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.background_image_cardview, parent, false);
        BackGroundViewHolder viewHolder = new BackGroundViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BackGroundViewHolder holder, final int position) {

        final String string = set.get(position);
        holder.setIsRecyclable(false);
        if (position <= 8) {
            holder.imageView.setBackgroundResource(Integer.valueOf(string));
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(inflater.getContext(), ConversationActivity.class);
                    intent.putExtra("messageId", messageId);
                    intent.putExtra("BackGroundImage", Integer.valueOf(string));
                    intent.putExtra("BackGroundImage" + "String", "");
                    inflater.getContext().startActivity(intent);
                }
            });
            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(inflater.getContext());
                    alert.setMessage("You can\'t delete system image!!");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                    return true;
                }
            });

        } else {
            Bitmap myBitmap = BitmapFactory.decodeFile(string);
            holder.imageView.setImageBitmap(myBitmap);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(inflater.getContext(), ConversationActivity.class);
                    intent.putExtra("messageId", messageId);
                    intent.putExtra("BackGroundImage" + "String", string);
                    inflater.getContext().startActivity(intent);
                }
            });
            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(inflater.getContext());
                    alert.setTitle("Delete Image");
                    alert.setMessage("Press \"OK\" to delete this image");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            set.remove(position);
                            Gson gson = new Gson();
                            SharedPreferences sharedPreference = inflater.getContext().getSharedPreferences(BACKGROUNDLIST, Context.MODE_PRIVATE);
                            String json1 = gson.toJson(set);
                            sharedPreference.edit().putString(BACKGROUNDLIST_FULL, json1).apply();
                            notifyDataSetChanged();
                            Toast.makeText(inflater.getContext(), "Image deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return set.size();
    }

}

class BackGroundViewHolder extends RecyclerView.ViewHolder{

    ImageView imageView;

    public BackGroundViewHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.background_thumb_image);
    }
}
