package com.test.augment.view;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.test.augment.R;
import com.test.augment.model.Repo;

public class RepoDetailsActivity extends AugmentBaseActivity {

    public static final String BUNDLE_KEY_REPO = "bundleKeyRepository"; // Repository passed to this activity

    Repo repository;

    ImageView ivOwnerImageRepoDetails;
    TextView tvOwnerTitleRepoDetails, tvOwnerUrlRepoDetails, tvTitleRepoDetails, tvDescRepoDetails, tvUrlRepoDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_details);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            repository = (Repo) bundle.getSerializable(BUNDLE_KEY_REPO);
        }

        ivOwnerImageRepoDetails = (ImageView) findViewById(R.id.ivOwnerImageRepoDetails);
        tvOwnerTitleRepoDetails = (TextView) findViewById(R.id.tvOwnerTitleRepoDetails);
        tvOwnerUrlRepoDetails = (TextView) findViewById(R.id.tvOwnerUrlRepoDetails);
        tvTitleRepoDetails = (TextView) findViewById(R.id.tvTitleRepoDetails);
        tvDescRepoDetails = (TextView) findViewById(R.id.tvDescRepoDetails);
        tvUrlRepoDetails = (TextView) findViewById(R.id.tvUrlRepoDetails);

        fillUI();
    }

    private void fillUI() {
        if(repository == null)
            return;
        tvTitleRepoDetails.setText(repository.name);
        if(repository.owner != null) {
            loadImage(repository.owner.avatar_url, R.drawable.ico_github_placeholder, ivOwnerImageRepoDetails);
            tvOwnerTitleRepoDetails.setText(repository.owner.login);
            tvOwnerUrlRepoDetails.setText(repository.owner.url);
        }

        tvTitleRepoDetails.setText(repository.name+" ("+repository.full_name+")");
        tvDescRepoDetails.setText(repository.description);
        tvUrlRepoDetails.setText(repository.url);
    }
}
