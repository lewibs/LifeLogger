use rand::prelude::*;
use rand::seq::SliceRandom; // gives .choose()
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

    // Sample data
    let first_names = vec![
        "Alice", "Bob", "Carol", "Dave", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Judy", "Karl",
        "Laura", "Mallory", "Niaj", "Olivia", "Peggy", "Rupert", "Sybil", "Trent", "Victor",
        "Wendy",
    ];
    let cities = vec![
        "New York",
        "San Francisco",
        "Chicago",
        "Austin",
        "Miami",
        "Seattle",
        "Denver",
        "Boston",
        "Los Angeles",
        "Portland",
    ];
    let companies = vec![
        "OpenAI",
        "Techtonic",
        "Globex",
        "Initech",
        "Hooli",
        "Cyberdyne",
        "Umbrella",
        "Wayne Enterprises",
        "Stark Industries",
        "Wonka Labs",
    ];
    let regions = vec!["North", "South", "East", "West", "Midwest"];
    let industries = vec!["Tech", "Finance", "Healthcare", "Retail", "Energy"];

    // Generate People
    for name in &first_names {
        let city = cities.choose(&mut rng).unwrap();
        let employer = companies.choose(&mut rng).unwrap();
        let age = rng.gen_range(20..65);
        let salary = rng.gen_range(40_000..150_000);

        let person = json!({
            "name": name,
            "age": age,
            "city": city,
            "employer": employer,
            "salary": salary,
        });

        let filename = format!("./data/{}.Person.json", name.to_lowercase());
        write_json(&filename, &person)?;
    }

    // Generate Cities
    for city_name in &cities {
        let region = regions.choose(&mut rng).unwrap();

        let city_data = json!({
            "name": city_name,
            "population": rng.gen_range(100_000..10_000_000),
            "region": region
        });

        let filename = format!(
            "./data/{}.City.json",
            city_name.replace(" ", "_").to_lowercase()
        );
        write_json(&filename, &city_data)?;
    }

    // Generate Companies
    for company_name in &companies {
        let industry = industries.choose(&mut rng).unwrap();

        let company_data = json!({
            "name": company_name,
            "industry": industry,
            "founded": rng.gen_range(1950..2024),
            "employee_count": rng.gen_range(50..10_000),
        });

        let filename = format!(
            "./data/{}.Company.json",
            company_name.replace(" ", "_").to_lowercase()
        );
        write_json(&filename, &company_data)?;
    }

    println!("✅ Generated sample data in ./data/");
    Ok(())
}

fn write_json(path: &str, value: &serde_json::Value) -> std::io::Result<()> {
    let mut file = fs::File::create(path)?;
    file.write_all(serde_json::to_string_pretty(value)?.as_bytes())?;
    Ok(())
}
