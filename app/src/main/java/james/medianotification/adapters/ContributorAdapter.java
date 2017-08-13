package james.medianotification.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import james.medianotification.R;
import james.medianotification.data.ContributorData;
import james.medianotification.views.CircleImageView;

public class ContributorAdapter extends RecyclerView.Adapter<ContributorAdapter.ViewHolder> {

    private List<ContributorData> contributors;

    public ContributorAdapter(List<ContributorData> contributors) {
        this.contributors = contributors;
    }

    @Override
    public ContributorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contributor, parent, false));
    }

    @Override
    public void onBindViewHolder(final ContributorAdapter.ViewHolder holder, int position) {
        ContributorData contributor = contributors.get(position);

        Glide.with(holder.imageView.getContext()).asBitmap().load(contributor.imageUrl).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                holder.imageView.setImageBitmap(resource);
            }
        });

        holder.textView.setText(contributor.name);

        holder.itemView.setTag(contributor);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null && view.getTag() instanceof ContributorData)
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((ContributorData) view.getTag()).url)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return contributors.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.title);
        }
    }
}
