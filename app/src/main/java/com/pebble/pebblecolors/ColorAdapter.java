package com.pebble.pebblecolors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by joe-work on 3/24/15.
 */
public class ColorAdapter extends ArrayAdapter<ColorCommand> {

    private static final String TAG = ColorAdapter.class.getSimpleName();

    private Context context;
    private List<ColorCommand> commandList;

    private class ViewHolder {
        LinearLayout rootView;
        TextView command;
        TextView commandType;
    }


    public ColorAdapter(final Context context, final List<ColorCommand> commands) {
        super(context, R.layout.list_color_item, commands);
        this.context = context;
        this.commandList = commands;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_color_item, parent, false);
            holder = new ViewHolder();
            holder.rootView = (LinearLayout) convertView.findViewById(R.id.root_list_item);
            holder.command = (TextView) convertView.findViewById(R.id.color_command_title);
            holder.commandType = (TextView) convertView.findViewById(R.id.color_command_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Get the object and set the object
        ColorCommand command = commandList.get(position);
        String commandString = String.format("R: %d G: %d B: %d", command.r, command.g, command.b);
        String commandTypeString;
        if (command.type == 0x01) {
            commandTypeString = "Relative";
        } else if (command.type == 0x02) {
            commandTypeString = "Absolute";
        } else {
            commandTypeString = "Debug";
        }

        holder.command.setText(commandString);
        holder.commandType.setText(commandTypeString);

        return convertView;
    }
}
