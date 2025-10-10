use rand::prelude::*;
use rand::seq::SliceRandom;
use serde_json::json;
use std::fs;
use std::io::Write;
use std::path::Path;

fn main() -> std::io::Result<()> {
    let data_dir = Path::new("./data");
    if !data_dir.exists() {
        fs::create_dir(data_dir)?;
    }

    let mut rng = rand::thread_rng();

    let types = vec!["memory", "conversation", "activity", "place", "person"];
    let moods = vec!["happy", "sad", "angry", "excited", "calm"];
    let topics = vec!["work", "family", "hobby", "travel", "food", "sports"];
    let activities = vec!["running", "reading", "coding", "gaming", "shopping"];
    let places = vec!["park", "restaurant", "home", "office", "beach"];
    let names = vec![
        "Alice", "Bob", "Carol", "Dave", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Judy",
    ];
    let cities = vec!["New York", "San Francisco", "Austin", "Miami"];

    let s3_base = "https://s3.fakebucket.com/";

    for i in 0..50 {
        let entry_type = types.choose(&mut rng).unwrap();

        // Pick all random values first
        let city = cities.choose(&mut rng).unwrap();
        let person1 = names.choose(&mut rng).unwrap();
        let person2 = names.choose(&mut rng).unwrap();
        let activity = activities.choose(&mut rng).unwrap();
        let place = places.choose(&mut rng).unwrap();
        let mood = moods.choose(&mut rng).unwrap();
        let topic = topics.choose(&mut rng).unwrap();
        let age = rng.gen_range(18..80);
        let duration_minutes = rng.gen_range(5..120);
        let visited_times = rng.gen_range(1..20);

        let metadata = match *entry_type {
            "memory" => json!({ "mood": mood, "topic": topic }),
            "conversation" => json!({ "participants": [person1, person2], "topic": topic }),
            "activity" => {
                json!({ "activity_type": activity, "duration_minutes": duration_minutes })
            }
            "place" => json!({ "name": place, "visited_times": visited_times }),
            "person" => json!({ "name": person1, "age": age, "city": city }),
            _ => json!({}),
        };

        let url = format!("{}{}.json", s3_base, i);

        let json_object = json!({
            "type": entry_type,
            "metadata": metadata,
            "url": url
        });

        let filename = format!("./data/{}.{}.json", i, entry_type);
        write_json(&filename, &json_object)?;
    }

    println!("✅ Generated random typed data in ./data/");
    Ok(())
}

fn write_json(path: &str, value: &serde_json::Value) -> std::io::Result<()> {
    let mut file = fs::File::create(path)?;
    file.write_all(serde_json::to_string_pretty(value)?.as_bytes())?;
    Ok(())
}
