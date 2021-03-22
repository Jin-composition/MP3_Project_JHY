package com.example.mp3_project_jhy;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.CustomViewHolder> {
    private Context context;
    private ArrayList<MusicData> musicList;

    //리스너 객체 참조를 저장하는 변수 (내부 인터페이스 멤버 변수)
    private OnItemClickListener mListener = null;

    //생성자 만들기
    public MusicAdapter(Context applicationContext){
        this.context = applicationContext;
    }

//    public MusicAdapter(Context context, ArrayList<MusicData> musicList) {
//        this.context = context;
//        this.musicList = musicList;
//    }


    @NonNull
    @Override
    public MusicAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        //화면 객체를 가져와서 뷰홀더에 저장
        View view = LayoutInflater.from(viewGroup.getContext()). inflate(R.layout.recycler_item, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
        public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int position) {

        //여기서 데이터를 제공
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

        //앨범 이미지를 비트맵으로 만들기
        Bitmap albumImg = getAlbumImg(context, Long.parseLong(musicList.get(position).getAlbumArt()), 200);
        if(albumImg != null){
            customViewHolder.albumArt.setImageBitmap(albumImg);
        }

        //이미지는 title, artist, duration 을 가져온다.
        customViewHolder.title.setText(musicList.get(position).getTitle());
        customViewHolder.artist.setText(musicList.get(position).getArtist());
        customViewHolder.duration.setText(sdf.format(Integer.parseInt(musicList.get(position).getDuration())));
    }

    @Override
    public int getItemCount() {
        return (musicList != null) ? musicList.size() : 0;
    }

    //앨범아트를 가져오는 함수
    public Bitmap getAlbumImg(Context context, long albumArt, int imgMaxSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = Uri.parse("content://media/external/audio/albumart/" + albumArt);

        if (uri != null){
            ParcelFileDescriptor fd = null;

            try {
                fd = contentResolver.openFileDescriptor(uri, "r");

                //메모리 할당을 하지 않으면서 해당된 정보를 읽어올 수 있다.
                options.inJustDecodeBounds = true;
                int scale = 0; //이미지 사이즈를 결정하기

                if(options.outHeight > imgMaxSize || options.outWidth > imgMaxSize){
                    scale = (int)Math.pow(2,(int) Math.round(Math.log(imgMaxSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }

                //비트맵을 위해서 메모리를 할당함함
                options.inJustDecodeBounds = false; // true면 비트맵을 만들지 않고 해당이미지의 가로, 세로 Mime type등의 정보만 가져옴
                options.inSampleSize = scale; // 이미지의 원본사이즈를 설정된 스케일로 축소

                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);

                if(bitmap != null){
                    //정확하게 사이즈를 맞춤
                    if(options.outWidth != imgMaxSize || options.outHeight != imgMaxSize){
                        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, imgMaxSize, imgMaxSize, true);
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                }
                return bitmap;
            } catch (FileNotFoundException e) {
                Log.d("MusicAdapter","컨텐트 리졸버 에러발생");
            }finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }


    //1. 내부 인터페이스를 정의한다.
    public interface OnItemClickListener {
        //추상화 메소드를 구현한다.
        void onItemClick(View view, int position);
    }

    //3. 내부 인터페이스 멤버변수에 대한 setters를 만든다.
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        //1. 내부 클래스 뷰홀더를 만든다.
        //화면에 보이는 리스트들만 먼저 만들어지고 위 아래로 드래그 시킬 때 화면에 안보이게 된 리스트 들은 없어지고
        //다시 올렸을 때 그 때 객체들이 대체 되는 형식
        private ImageView albumArt;
        private TextView title, artist, duration;



        //화면을 위로 올려 없어진 view 가 itemView에 온다.
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            albumArt = itemView.findViewById(R.id.d_ivAlbum);
            title = itemView.findViewById(R.id.d_tvTitle);
            artist = itemView.findViewById(R.id.d_tvArtist);
            duration = itemView.findViewById(R.id.d_tvDuration);

            //4. 구현한다.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        mListener.onItemClick(view, position);
                    }
                }
            });

        }
    }



    public void setMusicList(ArrayList<MusicData> musicList) {
        this.musicList = musicList;
    }
}
