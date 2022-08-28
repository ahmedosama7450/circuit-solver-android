package com.osamatech.circuitsolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

public class CircuitsAdapter extends RecyclerView.Adapter<CircuitsAdapter.CircuitViewHolder> {

    private CircuitItemClickListener circuitItemClickListener;
    private Context context;
    private int count;
    private SparseArray<Bitmap> cachedBitmaps = new SparseArray<>();

    public CircuitsAdapter(Context context, int count, CircuitItemClickListener circuitItemClickListener) {
        if (count > FakeCircuits.getCircuitsCount()) count = FakeCircuits.getCircuitsCount();
        this.count = count;
        this.context = context;
        this.circuitItemClickListener = circuitItemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull CircuitViewHolder circuitViewHolder, int i) {
        final ImageView imageView = circuitViewHolder.imageView;
        final int imageId = FakeCircuits.getCircuitImageId(i);
        Bitmap bitmap = cachedBitmaps.get(imageId);
        if (bitmap == null) {
            ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
            viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    Bitmap bitmap = ImageHelper.createSampledBitmap(context, imageId, imageView);
                    cachedBitmaps.append(imageId, bitmap);
                    imageView.setImageBitmap(bitmap);
                    return true;
                }
            });

        } else {
            imageView.setImageBitmap(bitmap);
        }
        TextView textView = circuitViewHolder.textView;
        textView.setText(FakeCircuits.getCircuitName(i));
    }

    @NonNull
    @Override
    public CircuitViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.circuit_item, viewGroup, false);
        return new CircuitViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    class CircuitViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        public CircuitViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_circuit_image);
            textView = itemView.findViewById(R.id.tv_circuit_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    circuitItemClickListener.onCircuitItemClicked(getAdapterPosition());
                }
            });
        }

    }

    interface CircuitItemClickListener {
        void onCircuitItemClicked(int position);
    }

}
