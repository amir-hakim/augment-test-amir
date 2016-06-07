package com.test.augment.view.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.test.augment.R;
import com.test.augment.model.Repo;
import com.test.augment.view.AugmentApplication;

import java.util.ArrayList;

public class RepoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    ArrayList<Repo> reposList;
    RecyclerViewEvents recyclerViewEvents;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvRepoName, tvRepoDesc, tvRepoOwner;
        public View view, llRepoItemContainer;
        public ViewHolder(View view) {
            super(view);
            tvRepoName = (TextView) view.findViewById(R.id.tvRepoName);
            tvRepoDesc = (TextView) view.findViewById(R.id.tvRepoDesc);
            tvRepoOwner = (TextView) view.findViewById(R.id.tvRepoOwner);
            llRepoItemContainer = view.findViewById(R.id.llRepoItemContainer);
            this.view = view;
        }
    }
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar)view.findViewById(R.id.progressBar1);
        }
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public RepoAdapter(ArrayList<Repo> reposList) {
        this.reposList = reposList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repo_item, parent, false);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder _holder, final int position) {
        if (_holder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) _holder;
            boolean isForked = reposList.get(position).fork;
            holder.tvRepoName.setText(reposList.get(position).name);
            holder.tvRepoDesc.setText(reposList.get(position).description);
            holder.tvRepoOwner.setText((reposList.get(position).owner != null ? (AugmentApplication.getContext().getString(R.string.by)+" "+reposList.get(position).owner.login) + " - " : "") + (AugmentApplication.getContext().getString(isForked ? R.string.forked : R.string.not_forked)));

            holder.view.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    if(recyclerViewEvents != null)
                        recyclerViewEvents.onRecyclerItemLongClick(position);
                    return false;
                }
            });
            holder.view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(recyclerViewEvents != null)
                        recyclerViewEvents.onRecyclerItemClick(position);
                }
            });
        } else if (_holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) _holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (reposList != null) ? reposList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return reposList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setRecyclerViewEvents(RecyclerViewEvents recyclerViewEvents) {
        this.recyclerViewEvents = recyclerViewEvents;
    }

    public void setReposList(ArrayList<Repo> reposList) {
        this.reposList = reposList;
    }

    public void removeRecyclerViewEvents() {
        setRecyclerViewEvents(null);
    }
}
