package com.example.tberroa.girodicerapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tberroa.girodicerapp.activities.PastInspectionsActivity;
import com.example.tberroa.girodicerapp.data.ClientId;
import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.models.Client;
import com.example.tberroa.girodicerapp.R;
import com.example.tberroa.girodicerapp.helpers.Utilities;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ClientManagerAdapter extends RecyclerView.Adapter<ClientManagerAdapter.ClientViewHolder> {

    private final Context context;
    private final List<Client> clients;

    public ClientManagerAdapter(Context context, List<Client> clients) {
        this.context = context;
        this.clients = clients;
    }

    @Override
    public int getItemCount() {
        if (clients != null) {
            return clients.size();
        } else {
            return 0;
        }
    }

    @Override
    public ClientViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context c = viewGroup.getContext();
        View v = LayoutInflater.from(c).inflate(R.layout.element_thumbnail, viewGroup, false);
        return new ClientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ClientViewHolder clientViewHolder, int i) {
        // set client name text
        String title = clients.get(i).user.first_name + "\n" + clients.get(i).user.last_name;
        clientViewHolder.clientName.setText(title);

        // construct google map url
        String address = clients.get(i).address;
        address = address.replaceAll(" ", "+").replaceAll(",", "+");
        String key = Params.GOOGLE_STATIC_MAPS_KEY;
        String baseUrl = Params.GOOGLE_STATIC_MAPS_URL;
        String url = baseUrl + "center=" + address + "&zoom=17&size=400x400&key=" + key + "&markers=color:red|" + address;

        // render thumbnail with Picasso
        int width = Utilities.getImageWidthGrid(context);
        int height = Utilities.getImageHeightGrid(context);
        Picasso.with(context).load(url).resize(width, height).into(clientViewHolder.addressImage);
    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {

        final ImageView addressImage;
        final TextView clientName;
        final RelativeLayout thumbnailLayout;

        ClientViewHolder(View itemView) {
            super(itemView);
            addressImage = (ImageView) itemView.findViewById(R.id.thumbnail);
            clientName = (TextView) itemView.findViewById(R.id.label);
            thumbnailLayout = (RelativeLayout) itemView.findViewById(R.id.thumbnail_layout);
            thumbnailLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // extract clicked client
                    int i = getLayoutPosition();
                    Client client = clients.get(i);

                    // save client id to shared preference
                    new ClientId().set(v.getContext(), client.id);

                    // start past inspections activity
                    context.startActivity(new Intent(v.getContext(), PastInspectionsActivity.class));
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }
            });
        }
    }
}