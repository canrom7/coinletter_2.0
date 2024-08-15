package com.iimm.miliao.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.circle.PublicMessage;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ScreenUtil;

import java.util.List;

/**
 * MrLiu253@163.com
 *
 * @time 2019-07-03
 */
public class MultiImageView extends LinearLayout {
    private static String TAG = "MultiImageView";
    public int MAX_WIDTH = 0;

    // 照片的Url列表
    private List<PublicMessage.Resource> imagesList;

    /**
     * 长度 单位为Pixel
     **/
    private int pxOneMaxWandH;  // 单张图最大允许宽高
    private int pxMoreWandH = 0;// 多张图的宽高
    private int pxImagePadding = DisplayUtil.dip2px(getContext(), 3f);// 图片间的间距

    private int MAX_PER_ROW_COUNT = 3;// 每行显示最大数

    private LayoutParams onePicPara;//一张图片布局参数
    private LayoutParams morePara, moreParaColumnFirst;//多张图的布局参数（非第一列、第一列）
    private LayoutParams rowPara;//行布局参数

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public MultiImageView(Context context) {
        super(context);
    }

    public MultiImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setList(List<PublicMessage.Resource> lists) throws IllegalArgumentException {
        if (lists == null) {
            throw new IllegalArgumentException("imageList is null...");
        }
        imagesList = lists;
        if (MAX_WIDTH > 0) {
            pxMoreWandH = (MAX_WIDTH - pxImagePadding * 2) / 3; //解决右侧图片和内容对不齐问题
            pxOneMaxWandH = MAX_WIDTH * 2 / 3;
            initImageLayoutParams();
        }

        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MAX_WIDTH == 0) {
            int width = measureWidth(widthMeasureSpec);
            if (width > 0) {
                MAX_WIDTH = width;
                //Log.i("info","图片显示器的宽度==="+width);
                if (imagesList != null && imagesList.size() > 0) {
                    setList(imagesList);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            // result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
            // + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private void initImageLayoutParams() {
        int wrap = LayoutParams.WRAP_CONTENT;
        int match = LayoutParams.MATCH_PARENT;

        onePicPara = new LayoutParams(wrap, wrap);

        moreParaColumnFirst = new LayoutParams(pxMoreWandH, pxMoreWandH);
        morePara = new LayoutParams(pxMoreWandH, pxMoreWandH);
        morePara.setMargins(pxImagePadding, 0, 0, 0);

        rowPara = new LayoutParams(match, wrap);
    }

    // 根据imageView的数量初始化不同的View布局,还要为每一个View作点击效果
    private void initView() {
        this.setOrientation(VERTICAL);
        this.removeAllViews();
        if (MAX_WIDTH == 0) {
            //为了触发onMeasure()来测量MultiImageView的最大宽度，MultiImageView的宽设置为match_parent
            addView(new View(getContext()));
            return;
        }

        if (imagesList == null || imagesList.size() == 0) {
            return;
        }

        if (imagesList.size() == 1) {
            addView(createImageView(0, false));
        } else {
            int allCount = imagesList.size();
            if (allCount == 4) {
                MAX_PER_ROW_COUNT = 2;
            } else {
                MAX_PER_ROW_COUNT = 3;
            }
            int rowCount = allCount / MAX_PER_ROW_COUNT
                    + (allCount % MAX_PER_ROW_COUNT > 0 ? 1 : 0);// 行数
            for (int rowCursor = 0; rowCursor < rowCount; rowCursor++) {
                LinearLayout rowLayout = new LinearLayout(getContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                rowLayout.setLayoutParams(rowPara);
                if (rowCursor != 0) {
                    rowLayout.setPadding(0, pxImagePadding, 0, 0);
                }

                int columnCount = allCount % MAX_PER_ROW_COUNT == 0 ? MAX_PER_ROW_COUNT
                        : allCount % MAX_PER_ROW_COUNT;//每行的列数
                if (rowCursor != rowCount - 1) {
                    columnCount = MAX_PER_ROW_COUNT;
                }
                addView(rowLayout);

                int rowOffset = rowCursor * MAX_PER_ROW_COUNT;// 行偏移
                for (int columnCursor = 0; columnCursor < columnCount; columnCursor++) {
                    int position = columnCursor + rowOffset;
                    rowLayout.addView(createImageView(position, true));
                }
            }
        }
    }

    private ImageView createImageView(int position, final boolean isMultiImage) {
        PublicMessage.Resource photoInfo = imagesList.get(position);
        final ImageView imageView = new ColorFilterImageView(getContext());
        if (isMultiImage) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(position % MAX_PER_ROW_COUNT == 0 ? moreParaColumnFirst : morePara);
            imageView.setBackgroundColor(getResources().getColor(R.color.chat_bottom_line));
        } else {
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setMaxHeight(pxOneMaxWandH);
            int expectW = 0;
            int expectH = 0;
            if (expectW == 0 || expectH == 0) {
                Log.i(TAG, "宽高设置 走这里===");
                imageView.setLayoutParams(onePicPara);
                //imageView.setLayoutParams(new LayoutParams(ScreenUtil.getScreenWidth(getContext())/2, ScreenUtil.getScreenHeight(getContext())/4));
            } else {
                int actualW = 0;
                int actualH = 0;
                float scale = ((float) expectH) / ((float) expectW);
                if (expectW > pxOneMaxWandH) {
                    actualW = pxOneMaxWandH;
                    actualH = (int) (actualW * scale);
                } else if (expectW < pxMoreWandH) {
                    actualW = pxMoreWandH;
                    actualH = (int) (actualW * scale);
                } else {
                    actualW = expectW;
                    actualH = expectH;
                }
                imageView.setLayoutParams(new LayoutParams(actualW, actualH));
            }
        }

        imageView.setId(photoInfo.getOriginalUrl().hashCode());
        imageView.setOnClickListener(new ImageOnClickListener(position));
        if (!isMultiImage) {
            Log.i(TAG, "走这里");
            final int screenWidth = ScreenUtil.getScreenWidth(getContext());
            final int screenHeight = ScreenUtil.getScreenHeight(getContext());
            LinearLayout.LayoutParams para = (LayoutParams) imageView.getLayoutParams();
            para.width = screenWidth / 3;
            para.height = screenWidth / 3;
            imageView.setImageResource(R.color.color_e6e6e6);
            Log.i(TAG, "photoInfo.url:" + photoInfo.getOriginalUrl());
            /*if (photoInfo.url.contains(".gif")){
                Glide.with(getContext()).load(photoInfo.url).asGif().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
			}else {*/
            //Glide.with(getContext()).load(photoInfo.url).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
            if (photoInfo.getOriginalUrl().endsWith(".gif")) {
                Glide.with(getContext())
                        //.load(new QiNiuImage(photoInfo.url))
                        .load(photoInfo.getOriginalUrl())
                        .asGif()
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.image_download_fail_icon)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .into(imageView);
            } else {
                Glide.with(getContext())
                        //.load(new QiNiuImage(photoInfo.url))
                        .load(photoInfo.getOriginalUrl())
                        .asBitmap()
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.image_download_fail_icon)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .centerCrop()
                        .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                int imageWidth = resource.getWidth();
                                int imageHeight = resource.getHeight();
                                int width = screenWidth * 1 / 2;
                                int height = screenHeight * 1 / 3;
                                LinearLayout.LayoutParams para = (LayoutParams) imageView.getLayoutParams();
                                para.gravity = Gravity.START;
                                if (imageHeight >= imageWidth) {
                                    para.height = height;
                                    para.width = imageWidth * height / imageHeight;
                                } else {
                                    para.width = width;
                                    para.height = imageHeight * width / imageWidth;
                                }
                                imageView.setImageBitmap(resource);
                            }
                        });
            }

            //}
            return imageView;
        }
        if (photoInfo.getOriginalUrl().endsWith(".gif")) {
            Glide.with(getContext()).load(photoInfo.getOriginalUrl()).asGif().diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().into(imageView);
        } else {
            Glide.with(getContext()).load(photoInfo.getOriginalUrl()).diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().into(imageView);
        }

        return imageView;
    }

    private class ImageOnClickListener implements OnClickListener {

        private int position;

        public ImageOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position);
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }
}