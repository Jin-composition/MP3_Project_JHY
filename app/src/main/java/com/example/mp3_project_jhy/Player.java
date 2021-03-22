package com.example.mp3_project_jhy;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Player extends Fragment implements View.OnClickListener{

    private ImageView ivAlbum;
    private TextView tvPlayCount, tvArtist, tvTitle, tvCurrentTime, tvDuration;
    private SeekBar seekBar;
    private ImageButton ibPlay, ibPrevious, ibNext, ibLike;

    private MainActivity mainActivity;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private int index;
    private MusicData musicData = new MusicData();
    private ArrayList<MusicData> likeArrayList = new ArrayList<>();
    private MusicAdapter musicAdapter;

    //MainActivity에서 Fragment 를 추가하면 호출됨
    //인자로 context 를 받아서 Listener interface 를 implement 한 경우에 Context 를 통해 가져올 수 있다
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)getActivity();
    }

    //Fragment 가 제거되고, Activity로부터 해제될 때 호출됩니다.
    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.player, container, false);

        //뷰 아이디
        findViewByIdFunc(view);

        //어댑터 가져옴
        musicAdapter = mainActivity.getMusicAdapter_like();
        //좋아요 리스트 가져오기
        likeArrayList = mainActivity.getMusicLikeArrayList();
        
        musicAdapter.setMusicList(likeArrayList);
        
        seekBarChangeMethod();
        

        return view;
    }

    //시크바 변경에 관한 함수
    private void seekBarChangeMethod() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean b) {
                //사용자가 움직였을 시, seekbar 이동
                if(b){
                    mediaPlayer.seekTo(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //시크바 스레드에 관한 함수
    private void setSeekBarThread(){
        Thread thread = new Thread(new Runnable() {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                while(mediaPlayer.isPlaying()){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvCurrentTime.setText(sdf.format(mediaPlayer.getCurrentPosition()));

                        }
                    });
                    SystemClock.sleep(100);
                }
            }
        });
        thread.start();
    }


    private void findViewByIdFunc(View view) {
        ivAlbum = view.findViewById(R.id.ivAlbum);
        tvPlayCount = view.findViewById(R.id.tvPlayCount);
        tvArtist = view.findViewById(R.id.tvArtist);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        tvDuration = view.findViewById(R.id.tvDuration);
        seekBar = view.findViewById(R.id.seekBar);
        ibPlay = view.findViewById(R.id.ibPlay);
        ibPrevious = view.findViewById(R.id.ibPrevious);
        ibNext = view.findViewById(R.id.ibNext);
        ibLike = view.findViewById(R.id.ibLike);

        ibPlay.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibLike.setOnClickListener(this);
    }

    //버튼 클릭 이벤트처리 함수
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.ibPlay :
                if(ibPlay.isActivated()){
                    mediaPlayer.pause();
                    ibPlay.setActivated(false);
                }else{
                    mediaPlayer.start();

                    setSeekBarThread();
                }
                break;
            case R.id.ibPrevious :
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    if(index == 0){
                        index = mainActivity.getMusicDataArrayList().size();
                    }
                    index--;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("ubPrevious",e.getMessage());
                }
                break;
            case R.id.ibNext :
                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    if(index == mainActivity.getMusicDataArrayList().size()-1){
                        index= -1;
                    }
                    index++;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("ibNext",e.getMessage());
                }
                break;
            case R.id.ibLike :

                if(ibLike.isActivated()){
                    ibLike.setActivated(false);
                    musicData.setLiked(0);
                    likeArrayList.remove(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(mainActivity, "좋아요 취소", Toast.LENGTH_SHORT).show();

                }else{
                    ibLike.setActivated(true);
                    musicData.setLiked(1);
                    likeArrayList.add(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(mainActivity, "좋아요", Toast.LENGTH_SHORT).show();
                }

                break;
            default:break;
        }
    }

    public void setPlayerData(int pos, boolean b) {
    }
}
