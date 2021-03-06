package com.example.mars.adapters;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mars.R;
import com.example.mars.entities.Post;

import java.util.List;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private List<Post> posts;
    private LayoutInflater mInflater;
    private Context context;

    TextToSpeech tts;

    final int VOICE_RECOGNITION = 3;
    String mostRecentUtteranceID;
    int count = 0;

    public PostsAdapter(Context context, List<Post> data) {
        this.mInflater = LayoutInflater.from(context);
        this.posts = data;
        this.context = context;

        initTTS();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorName;
        ImageView authorAvatar;
        TextView title;
        TextView desc;
        TextView createdAt;
        TextView menuBtn;

        ViewHolder(View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.authorName);
            authorAvatar = itemView.findViewById(R.id.authorAvatar);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            createdAt = itemView.findViewById(R.id.createdAt);
            menuBtn = itemView.findViewById(R.id.menuBtn);
        }
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.authorName.setText(post.authorName);
        holder.title.setText(post.title);
        holder.desc.setText(post.desc);
        holder.createdAt.setText(post.formattedCreationDate());

        Glide.with(context)
                .load(post.authorAvatar)
                .centerCrop()
                .circleCrop()
                .into(holder.authorAvatar);

        holder.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MarsTag", "onClick: ");
                PopupMenu popupMenu = new PopupMenu(context, holder.menuBtn);
                popupMenu.inflate(R.menu.post_options_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_item_read:
                                speakText(post);
                                break;
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void speakText(Post post) {
        try {
            String TTS = post.formattedTTS();

            count++;
            mostRecentUtteranceID = Integer.toString(count) + " ID";
            tts.speak(TTS, TextToSpeech.QUEUE_ADD, null, mostRecentUtteranceID);
        } catch(Exception e) {

        }
    }

    private void initTTS() {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.ENGLISH);
                    tts.setPitch((float) 1);
                    tts.setSpeechRate((float) 1);
                }
            }
        });
    }
}