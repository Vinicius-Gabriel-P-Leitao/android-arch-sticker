/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package br.arch.sticker.view.feature.preview.adapter;

import static br.arch.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MAX;
import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_ANIMATED;
import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_STATIC;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.webp.decoder.WebpDrawable;
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import br.arch.sticker.R;
import br.arch.sticker.core.util.BuildStickerUri;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.core.util.transformation.CropSquareTransformation;
import br.arch.sticker.view.feature.preview.viewholder.AddNewStickerViewHolder;
import br.arch.sticker.view.feature.preview.viewholder.InvalidStickerButtonPreviewViewHolder;
import br.arch.sticker.view.feature.preview.viewholder.StickerPreviewViewHolder;

public class StickerPreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnEventClickedListener {
        void onInvalidStickerClicked();

        void onNewSticker();

        void onStickerSelected(Sticker sticker);
    }

    private static final float COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA = 1f;
    private static final float EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA = 0.2f;
    private static final int VIEW_TYPE_STICKER = 0;
    private static final int VIEW_TYPE_BUTTON = 1;
    private static final int VIEW_TYPE_NEW_STICKER = 2;

    private Set<Pair<String, String>> selectedIds = new HashSet<>();
    private boolean isDeleteMode = false;
    private static StickerPack stickerPack;

    private final ArrayList<Sticker> invalidStickers;
    private final ArrayList<Sticker> stickerList;

    private final int errorResource;
    private final int cellPadding;
    private final int cellSize;

    private final ImageView expandedStickerPreview;
    private final LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private View clickedStickerPreview;
    private final Context context;
    float expandedViewLeftX;
    float expandedViewTopY;

    private OnEventClickedListener invalidStickerClickListener;

    private final RecyclerView.OnScrollListener hideExpandedViewScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx != 0 || dy != 0) {
                hideExpandedStickerPreview();
            }
        }
    };

    public StickerPreviewAdapter(Context context, @NonNull final LayoutInflater layoutInflater, final int errorResource, final int cellSize, final int cellPadding, @NonNull final StickerPack stickerPack, @NonNull ArrayList<Sticker> invalidStickers, final ImageView expandedStickerView, OnEventClickedListener invalidStickerClickListener) {
        this.context = context;
        this.cellSize = cellSize;
        this.cellPadding = cellPadding;
        this.layoutInflater = layoutInflater;
        this.errorResource = errorResource;
        this.invalidStickers = invalidStickers;
        StickerPreviewAdapter.stickerPack = stickerPack;
        this.expandedStickerPreview = expandedStickerView;
        this.invalidStickerClickListener = invalidStickerClickListener;

        this.stickerList = filterValidStickers(StickerPreviewAdapter.stickerPack);
    }

    public StickerPreviewAdapter(Context context, @NonNull final LayoutInflater layoutInflater, final int errorResource, final int cellSize, final int cellPadding, @NonNull final StickerPack stickerPack, @NonNull ArrayList<Sticker> invalidStickers, final ImageView expandedStickerView) {
        this.context = context;
        this.cellSize = cellSize;
        this.cellPadding = cellPadding;
        this.errorResource = errorResource;
        this.layoutInflater = layoutInflater;
        this.invalidStickers = invalidStickers;
        StickerPreviewAdapter.stickerPack = stickerPack;
        this.expandedStickerPreview = expandedStickerView;

        this.stickerList = filterValidStickers(stickerPack);
    }

    public void setDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
        if (viewType == VIEW_TYPE_BUTTON) {
            View itemView = layoutInflater.inflate(R.layout.button_invalid_item_preview, viewGroup, false);
            InvalidStickerButtonPreviewViewHolder invalidStickerButtonPreviewViewHolder = new InvalidStickerButtonPreviewViewHolder(
                    itemView);

            ViewGroup.LayoutParams layoutParams = invalidStickerButtonPreviewViewHolder.materialButton.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;

            invalidStickerButtonPreviewViewHolder.materialButton.setLayoutParams(layoutParams);
            invalidStickerButtonPreviewViewHolder.materialButton.setPadding(cellPadding, cellPadding, cellPadding,
                    cellPadding);

            return invalidStickerButtonPreviewViewHolder;
        }

        if (viewType == VIEW_TYPE_NEW_STICKER) {
            View itemView = layoutInflater.inflate(R.layout.button_add_new_sticker, viewGroup, false);
            AddNewStickerViewHolder addNewStickerViewHolder = new AddNewStickerViewHolder(itemView);

            ViewGroup.LayoutParams layoutParams = addNewStickerViewHolder.materialButton.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;

            addNewStickerViewHolder.materialButton.setLayoutParams(layoutParams);
            addNewStickerViewHolder.materialButton.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);

            return addNewStickerViewHolder;
        }

        View itemView = layoutInflater.inflate(R.layout.preview_sticker_icon_details, viewGroup, false);
        StickerPreviewViewHolder stickerPreviewViewHolder = new StickerPreviewViewHolder(itemView);

        ViewGroup.LayoutParams layoutParams = stickerPreviewViewHolder.stickerPreviewView.getLayoutParams();
        layoutParams.height = cellSize;
        layoutParams.width = cellSize;

        stickerPreviewViewHolder.stickerPreviewView.setLayoutParams(layoutParams);
        stickerPreviewViewHolder.stickerPreviewView.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);

        return stickerPreviewViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof StickerPreviewViewHolder previewViewHolder) {
            Sticker sticker = stickerList.get(position);

            previewViewHolder.stickerPreviewView.setImageResource(errorResource);
            previewViewHolder.stickerPreviewView.setImageURI(
                    BuildStickerUri.buildStickerAssetUri(stickerPack.identifier, sticker.imageFileName));
            previewViewHolder.stickerPreviewView.setOnClickListener(view -> {
                if (!isDeleteMode) {
                    expandPreview(position, previewViewHolder.stickerPreviewView);
                }
            });

            previewViewHolder.stickerPreviewView.setOnClickListener(view -> {
                if (isDeleteMode) {
                    if (invalidStickerClickListener != null) {
                        invalidStickerClickListener.onStickerSelected(sticker);
                    }
                } else {
                    expandPreview(position, previewViewHolder.stickerPreviewView);
                }
            });

            markSticker(previewViewHolder, new Pair<>(stickerPack.identifier, sticker.imageFileName));
        }

        if (viewHolder instanceof InvalidStickerButtonPreviewViewHolder invalidStickerButtonPreviewViewHolder) {
            invalidStickerButtonPreviewViewHolder.materialButton.setOnClickListener(view -> {
                if (invalidStickerClickListener != null) {
                    invalidStickerClickListener.onInvalidStickerClicked();
                }
            });
        }

        if (viewHolder instanceof AddNewStickerViewHolder addNewHolder) {
            addNewHolder.materialButton.setOnClickListener(view -> {
                if (invalidStickerClickListener != null) {
                    invalidStickerClickListener.onNewSticker();
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(hideExpandedViewScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(hideExpandedViewScrollListener);
        this.recyclerView = null;
    }

    @Override
    public int getItemCount() {
        int validCount = stickerList.size();
        int invalidCount = invalidStickers.size();
        int totalCount = validCount + invalidCount;

        if (totalCount < STICKER_SIZE_MAX) {
            return totalCount + 1;
        } else {
            return totalCount;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int validCount = stickerList.size();
        int invalidCount = invalidStickers.size();
        int totalCount = validCount + invalidCount;

        if (position < validCount) {
            return VIEW_TYPE_STICKER;
        } else if (position < validCount + invalidCount) {
            return VIEW_TYPE_BUTTON;
        } else {
            return VIEW_TYPE_NEW_STICKER;
        }
    }

    private static ArrayList<Sticker> filterValidStickers(@NonNull StickerPack stickerPack) {
        ArrayList<Sticker> result = new ArrayList<>();
        for (Sticker sticker : stickerPack.getStickers()) {
            if (!PLACEHOLDER_ANIMATED.equals(sticker.imageFileName) && !PLACEHOLDER_STATIC.equals(
                    sticker.imageFileName)) {
                result.add(sticker);
            }
        }
        return result;
    }

    private void markSticker(StickerPreviewViewHolder viewHolder, Pair<String, String> stickerDataPair) {
        boolean isSelected = selectedIds.contains(stickerDataPair);
        viewHolder.itemView.setBackgroundColor(
                isSelected ? ContextCompat.getColor(context, R.color.catppuccin_surface2) : Color.TRANSPARENT);
    }

    private void positionExpandedStickerPreview(int selectedPosition) {
        if (expandedStickerPreview != null) {
            final ViewGroup.MarginLayoutParams recyclerViewLayoutParams = ((ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams());
            final int recyclerViewLeftMargin = recyclerViewLayoutParams.leftMargin;
            final int recyclerViewRightMargin = recyclerViewLayoutParams.rightMargin;
            final int recyclerViewWidth = recyclerView.getWidth();
            final int recyclerViewHeight = recyclerView.getHeight();

            final StickerPreviewViewHolder clickedViewHolder = (StickerPreviewViewHolder) recyclerView.findViewHolderForAdapterPosition(
                    selectedPosition);

            if (clickedViewHolder == null) {
                hideExpandedStickerPreview();
                return;
            }

            clickedStickerPreview = clickedViewHolder.itemView;
            final float clickedViewCenterX = clickedStickerPreview.getX() + recyclerViewLeftMargin + clickedStickerPreview.getWidth() / 2f;
            final float clickedViewCenterY = clickedStickerPreview.getY() + clickedStickerPreview.getHeight() / 2f;

            expandedViewLeftX = clickedViewCenterX - expandedStickerPreview.getWidth() / 2f;
            expandedViewTopY = clickedViewCenterY - expandedStickerPreview.getHeight() / 2f;

            expandedViewLeftX = Math.max(expandedViewLeftX, 0);
            expandedViewTopY = Math.max(expandedViewTopY, 0);

            final float adjustmentX = Math.max(
                    expandedViewLeftX + expandedStickerPreview.getWidth() - recyclerViewWidth - recyclerViewRightMargin,
                    0);
            final float adjustmentY = Math.max(
                    expandedViewTopY + expandedStickerPreview.getHeight() - recyclerViewHeight, 0);

            expandedViewLeftX -= adjustmentX;
            expandedViewTopY -= adjustmentY;

            expandedStickerPreview.setX(expandedViewLeftX);
            expandedStickerPreview.setY(expandedViewTopY);
        }
    }

    private void expandPreview(int position, View clickedStickerPreview) {
        if (isStickerPreviewExpanded()) {
            hideExpandedStickerPreview();
            return;
        }

        this.clickedStickerPreview = clickedStickerPreview;

        if (expandedStickerPreview != null) {
            positionExpandedStickerPreview(position);

            String imageFileName = stickerPack.getStickers().get(position).imageFileName;

            final Uri stickerAssetUri = BuildStickerUri.buildStickerAssetUri(stickerPack.identifier, imageFileName);
            String extension = imageFileName.substring(imageFileName.lastIndexOf(".") + 1);

            boolean isAnimatedWebp = false;

            if (extension.equals("webp") && android.os.Build.VERSION.SDK_INT >= 28) {
                try {
                    Drawable drawable = Drawable.createFromStream(
                            expandedStickerPreview.getContext().getContentResolver().openInputStream(stickerAssetUri),
                            null);
                    if (drawable instanceof android.graphics.drawable.AnimatedImageDrawable) {
                        isAnimatedWebp = true;
                    }
                } catch (Exception exception) {
                    // NOTE: Ignora para assumir que não é animado
                }
            }

            MultiTransformation<Bitmap> commonTransform = new MultiTransformation<>(
                    new CropSquareTransformation(10f, 5, R.color.catppuccin_overlay2));

            RequestOptions requestOptions = new RequestOptions().override(300, 300);
            RequestManager glide = Glide.with(expandedStickerPreview.getContext());

            if (extension.equals("webp") && isAnimatedWebp) {
                expandedStickerPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                glide.load(stickerAssetUri).apply(requestOptions).error(R.drawable.sticker_3rdparty_warning)
                        .transform(WebpDrawable.class, new WebpDrawableTransformation(commonTransform))
                        .into(expandedStickerPreview);
            } else {
                glide.asBitmap().load(stickerAssetUri).apply(requestOptions).error(R.drawable.sticker_3rdparty_warning)
                        .centerCrop().transform(commonTransform).into(expandedStickerPreview);
            }

            expandedStickerPreview.setVisibility(View.VISIBLE);
            recyclerView.setAlpha(EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA);

            expandedStickerPreview.setOnClickListener(view -> hideExpandedStickerPreview());
        }
    }

    public void hideExpandedStickerPreview() {
        if (isStickerPreviewExpanded() && expandedStickerPreview != null) {
            clickedStickerPreview.setVisibility(View.VISIBLE);
            expandedStickerPreview.setVisibility(View.INVISIBLE);
            recyclerView.setAlpha(COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA);
        }
    }

    private boolean isStickerPreviewExpanded() {
        return expandedStickerPreview != null && expandedStickerPreview.getVisibility() == View.VISIBLE;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedIds(Set<Pair<String, String>> stickerDataPair) {
        selectedIds = new HashSet<>(stickerDataPair);
        notifyDataSetChanged();
    }
}
