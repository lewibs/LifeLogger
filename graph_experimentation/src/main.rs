async fn create_node(
    graph: &Graph,
    node_type: &str,
    props: &Value,
) -> Result<(), Box<dyn std::error::Error>> {
    // Extract url
    let url = props.get("url").and_then(|v| v.as_str()).unwrap_or("");

    // Extract metadata
    let metadata = props
        .get("metadata")
        .unwrap_or(&Value::Object(serde_json::Map::new()));

    // Build flat map of params Neo4rs can accept
    let mut params = std::collections::HashMap::new();

    // Add metadata
    if let Some(map) = metadata.as_object() {
        for (k, v) in map {
            if v.is_string() {
                params.insert(k.clone(), v.as_str().unwrap().to_string().into());
            } else if v.is_i64() {
                params.insert(k.clone(), v.as_i64().unwrap().into());
            } else if v.is_f64() {
                params.insert(k.clone(), v.as_f64().unwrap().into());
            } else if v.is_boolean() {
                params.insert(k.clone(), v.as_bool().unwrap().into());
            } else {
                // Serialize anything else to string
                params.insert(k.clone(), serde_json::to_string(v)?.into());
            }
        }
    }

    // Add url
    params.insert("url".to_string(), url.to_string().into());

    // Build query
    let keys: Vec<String> = params.keys().map(|k| format!("{}: ${}", k, k)).collect();
    let query_str = format!(
        "CREATE (n:{} {{ {} }}) RETURN n",
        node_type,
        keys.join(", ")
    );

    let mut query = Query::new(query_str);

    // Add params
    for (k, v) in params {
        query = query.param(k, v);
    }

    graph.run(query).await?;
    println!("✅ Created node '{}'", node_type);

    Ok(())
}
