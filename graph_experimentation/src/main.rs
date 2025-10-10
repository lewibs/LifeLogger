use neo4rs::*;
use serde_json::Value;
use std::error::Error;
use std::fs;
use std::path::Path;

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    // --- Configure Neo4j connection ---
    let uri = "bolt://127.0.0.1:7687";
    let user = "neo4j";
    let pass = "kodahasnoballs"; // your Neo4j password

    let graph = Graph::new(uri, user, pass).await?;
    let data_dir = Path::new("./data");

    // --- Read all JSON files in ./data ---
    for entry in fs::read_dir(data_dir)? {
        let entry = entry?;
        let path = entry.path();

        if path.extension().and_then(|s| s.to_str()) == Some("json") {
            if let Some(file_name) = path.file_name().and_then(|s| s.to_str()) {
                if let Some((name, label)) = parse_name_and_label(file_name) {
                    let json_data = fs::read_to_string(&path)?;
                    let properties: Value = serde_json::from_str(&json_data)?;

                    println!("📦 Creating node '{}' with label '{}'", name, label);
                    create_node(&graph, &label, &properties).await?;
                } else {
                    eprintln!("⚠️ Skipping file with invalid name format: {}", file_name);
                }
            }
        }
    }

    Ok(())
}

// Extracts ("name", "Label") from "name.label.json"
fn parse_name_and_label(file_name: &str) -> Option<(String, String)> {
    let parts: Vec<&str> = file_name.split('.').collect();
    if parts.len() == 3 && parts[2] == "json" {
        Some((parts[0].to_string(), parts[1].to_string()))
    } else {
        None
    }
}

async fn create_node(graph: &Graph, label: &str, properties: &Value) -> Result<(), Box<dyn Error>> {
    let obj = properties.as_object().expect("JSON must be an object");

    // Build dynamic Cypher query
    let keys: Vec<String> = obj.keys().map(|k| format!("{}: ${}", k, k)).collect();
    let query_str = format!("CREATE (n:{} {{ {} }}) RETURN n", label, keys.join(", "));

    let mut query = Query::new(query_str);

    // Add parameters
    for (k, v) in obj.iter() {
        let key = k.as_str();
        match v {
            Value::String(s) => query = query.param(key, s.clone()),
            Value::Number(num) if num.is_i64() => query = query.param(key, num.as_i64().unwrap()),
            Value::Number(num) if num.is_f64() => query = query.param(key, num.as_f64().unwrap()),
            Value::Bool(b) => query = query.param(key, *b),
            _ => eprintln!("⚠️ Skipping unsupported field: {}", key),
        }
    }

    graph.run(query).await?;
    println!("✅ Created node with label '{}'", label);
    Ok(())
}
