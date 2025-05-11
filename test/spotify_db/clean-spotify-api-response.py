import json

def convert_milliseconds_to_time(milliseconds: int):
    total_seconds = milliseconds // 1000
    minutes = total_seconds // 60
    seconds = total_seconds % 60
    return f"{minutes:02d}:{seconds:02d}"

def clean_spotify_json(input_path: str, output_path: str, playlist_name: str):
    with open(input_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    cleaned_items = []
    for item in data.get("items", []):
        track = item.get("track", {})
        album = track.get("album", {})
        artists = track.get("artists", [])

        added_at_date = item.get("added_at", "")
        duration = int(track.get("duration_ms", 0))

        cleaned = {
            "music": track.get("name"),
            "artist": ",".join(artist.get("name", "") for artist in artists),
            "popularity": int(track.get("popularity", 0)),
            "album": album.get("name"),
            "duration": convert_milliseconds_to_time(duration),
            "added_at": added_at_date.split("T")[0] if added_at_date else None,
            "spotify_track_id": track.get("id"),
            "playlist_name": playlist_name,
            "album_year": int(album.get("release_date").split("-")[0]) if album.get("release_date") else None,
        }
        cleaned_items.append(cleaned)
    #print number of items
    print(f"Number of items: {len(cleaned_items)}")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(cleaned_items, f, indent=2, ensure_ascii=False)


# get the file name from the command line
if __name__ == "__main__":
    import sys
    if len(sys.argv) != 3:
        print("Usage: python clean-spotify-api-response.py <input_file> <playlist_name>")
        sys.exit(1)

    input_file = sys.argv[1]
    playlist_name = sys.argv[2]
    output_file = input_file.replace(".json", "-cleaned.json")

    clean_spotify_json(input_file, output_file, playlist_name)