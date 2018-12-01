package cmsc436.feelingsdiary;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/* Adapter for mResultsList in SearchActivity */
public class SearchListAdapter extends BaseAdapter {

    private ArrayList<Entry> list = new ArrayList<>();
    private static LayoutInflater inflater;
    private Context context;

    public SearchListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = convertView;
        ViewHolder holder;

        Entry entry = list.get(position);

        // ViewHolder stuff for better scrolling
        if (convertView == null) {
            holder = new ViewHolder();
            newView = inflater.inflate(R.layout.entry_list_layout, parent, false);

            holder.datetime = newView.findViewById(R.id.datetime);
            holder.moodRating = newView.findViewById(R.id.mood);
            holder.message = newView.findViewById(R.id.message_string);
            holder.tags = newView.findViewById(R.id.tags_string);
            newView.setTag(holder);
        } else {
            holder = (ViewHolder) newView.getTag();
        }

        // Set appropriate data in the View
        holder.datetime.setText(context.getString(R.string.datetime) + entry.getDate());
        holder.moodRating.setText(context.getString(R.string.mood_rating) + entry.getRating());

        List<String> tags = entry.getTags();
        StringBuilder tag;
        if (tags == null) {
            tag = new StringBuilder("None");
        } else {
            tag = new StringBuilder(tags.get(0).trim());
            for (int i = 1; i < tags.size(); i++) {
                tag.append(", ").append(tags.get(i).trim());
            }
        }
        if (tag.length() > 26) {
            holder.tags.setText(context.getString(R.string.tags_string) + tag.substring(0, 26) + "...");
        } else {
            holder.tags.setText(context.getString(R.string.tags_string) + tag);
        }

        if (entry.getEntry().length() == 0) {
            holder.message.setText(context.getString(R.string.message) + "None");
        } else if (entry.getEntry().length() > 26) {
            holder.message.setText(context.getString(R.string.message) + entry.getEntry().substring(0, 26) + "...");
        } else {
            holder.message.setText(context.getString(R.string.message) + entry.getEntry());
        }

        // Changing color of entry result based on the mood

        int rating = Integer.parseInt(entry.getRating());
        if (rating >= 4) {
            holder.datetime.setTextColor(Color.parseColor("#88D7BF"));
            holder.message.setTextColor(Color.parseColor("#88D7BF"));
            holder.moodRating.setTextColor(Color.parseColor("#88D7BF"));
            holder.tags.setTextColor(Color.parseColor("#88D7BF"));
        } else if (rating > 2) {
            holder.datetime.setTextColor(Color.parseColor("#EEC964"));
            holder.message.setTextColor(Color.parseColor("#EEC964"));
            holder.moodRating.setTextColor(Color.parseColor("#EEC964"));
            holder.tags.setTextColor(Color.parseColor("#EEC964"));
        } else {
            holder.datetime.setTextColor(Color.parseColor("#D06A74"));
            holder.message.setTextColor(Color.parseColor("#D06A74"));
            holder.moodRating.setTextColor(Color.parseColor("#D06A74"));
            holder.tags.setTextColor(Color.parseColor("#D06A74"));
        }

        return newView;
    }

    // ViewHolder for better scrolling
    private static class ViewHolder {
        TextView datetime;
        TextView moodRating;
        TextView message;
        TextView tags;
    }

    public void clearList() {
        list.clear();
        notifyDataSetInvalidated();
    }

    public void addList(List<Entry> list) {
        this.list.addAll(list);
        notifyDataSetInvalidated();
    }
}
