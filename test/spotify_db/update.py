# Print the updated data
import json

#get year from YYYY-MM-DD
def update(data, playlists):
    for group in data:
        # for each group get the name of the playlist (index of the group)
        playlist = playlists[data.index(group)]
        for item in group:
            item['playlist_name'] = playlist
            date = item.get("album_date", "")
            year = item.get("album_year", "")
            if date and year == "":
                year = date.split("-")[0]
                item["album_year"] = year
            milliseconds = item.get("time", 0)
            if milliseconds:
                total_seconds = milliseconds // 1000
                minutes = total_seconds // 60
                seconds = total_seconds % 60
                item["duration"] = f"{minutes:02d}:{seconds:02d}"
            added_at_date = item.get("added_at", "")
            # transform iso string into added_at to YYYY-MM-DD
            if added_at_date:
                date = added_at_date.split("T")[0]
                item["added_at"] = date
            
 
# get the file name from the command line
if __name__ == "__main__":
    import sys
    if len(sys.argv) != 3:
        print("Usage: python update.py <input_file> <playlists>")
        sys.exit(1)

    input_file = sys.argv[1]
    playlists = sys.argv[2].split(",")
    output_file = input_file.replace(".json", "-updated.json")
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    #convert_milliseconds_to_time(data)
    update(data,playlists=playlists)
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    #print number of items
    print(f"Number of items: {len(data)}")