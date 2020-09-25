package com.design2net.the_house.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.design2net.the_house.R;
import com.design2net.the_house.models.Producto;

import java.util.ArrayList;

public class ProductosRecyclerViewAdapter extends RecyclerView.Adapter<ProductosRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<Producto> mListaProductos;
    private OnProductoListener mOnProductoListener;

    public ProductosRecyclerViewAdapter(ArrayList<Producto> productos, OnProductoListener onProductoListener) {
        this.mListaProductos = productos;
        this.mOnProductoListener = onProductoListener;
    }

    public interface OnProductoListener {
        void onProductoClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View vista = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_product, viewGroup, false);

        return new ViewHolder(vista, mOnProductoListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.mProducto = mListaProductos.get(position);
        viewHolder.txtProductoDescription.setText(mListaProductos.get(position).getDescription()
                + "\n " + mListaProductos.get(position).getSku());
        viewHolder.txtProductoQty.setText(Integer.toString(mListaProductos.get(position).getPickQty()));
        viewHolder.txtViewVerificados.setText(Integer.toString(mListaProductos.get(position).getCheckQty()));
        viewHolder.txtViewOrderQty.setText(Integer.toString(mListaProductos.get(position).getOrderQty()));

        if (mListaProductos.get(position).isCorrect()) {
            viewHolder.txtProductoDescription.setBackgroundResource(R.color.good);
            viewHolder.txtProductoQty.setBackgroundResource(R.color.good);
            viewHolder.txtViewVerificados.setBackgroundResource(R.color.good);
            viewHolder.txtViewOrderQty.setBackgroundResource(R.color.good);
        }else if (mListaProductos.get(position).getCheckQty() >= 1) {
            viewHolder.txtProductoDescription.setBackgroundResource(R.color.wrong);
            viewHolder.txtProductoQty.setBackgroundResource(R.color.wrong);
            viewHolder.txtViewVerificados.setBackgroundResource(R.color.wrong);
            viewHolder.txtViewOrderQty.setBackgroundResource(R.color.wrong);
        }else {
            viewHolder.txtProductoDescription.setBackgroundResource(R.color.trasparent);
            viewHolder.txtProductoQty.setBackgroundResource(R.color.trasparent);
            viewHolder.txtViewVerificados.setBackgroundResource(R.color.trasparent);
            viewHolder.txtViewOrderQty.setBackgroundResource(R.color.trasparent);
        }

        viewHolder.setData(position);
    }

    @Override
    public int getItemCount() {
        return mListaProductos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        TextView txtProductoDescription;
        TextView txtProductoQty;
        TextView txtViewVerificados;
        TextView txtViewOrderQty;
        Producto mProducto;
        OnProductoListener onProductoListener;

        ViewHolder(@NonNull View itemView, OnProductoListener onProductoListener) {
            super(itemView);

            this.mView = itemView;
            this.txtProductoDescription = itemView.findViewById(R.id.txtViewDescription);
            this.txtProductoQty = itemView.findViewById(R.id.edtCheckQty);
            this.txtViewVerificados = itemView.findViewById(R.id.txtViewVerificados);
            this.txtViewOrderQty = itemView.findViewById(R.id.txtViewOrderQty);
            this.onProductoListener = onProductoListener;
        }

        void setData(final int position) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != RecyclerView.NO_POSITION) {
                        onProductoListener.onProductoClick(position);
                    }
                }
            });
        }
    }
}
