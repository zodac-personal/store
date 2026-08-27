const fs = require('fs');
const path = require('path');

// Products aren't modelled as independent sample data: the brief has no separate concept of a
// product catalogue distinct from what customers already ordered, so this derives the product
// list from the order descriptions already committed in data.sql (each order's description is
// treated as the description of the single product it contains), instead of generating new
// random values that wouldn't line up with the seeded orders.
const dataSqlPath = path.join(__dirname, '..', 'src', 'main', 'resources', 'db', 'changelog', 'data.sql');
const dataSql = fs.readFileSync(dataSqlPath, 'utf8');

const orderPattern = /INSERT INTO "order" \(id, description, customer_id\) VALUES \((\d+), '((?:[^'\\]|\\.)*)', \d+\);/g;

const orders = [];
let match;
while ((match = orderPattern.exec(dataSql)) !== null) {
    orders.push({ orderId: Number(match[1]), description: match[2] });
}

const productIdByDescription = new Map();
let nextProductId = 1;
for (const order of orders) {
    if (!productIdByDescription.has(order.description)) {
        productIdByDescription.set(order.description, nextProductId++);
    }
}

for (const [description, id] of productIdByDescription) {
    console.log(`INSERT INTO product (id, description) VALUES (${id}, '${description.replace(/'/g, "''")}');`);
}

for (const order of orders) {
    const productId = productIdByDescription.get(order.description);
    console.log(`INSERT INTO order_product (order_id, product_id) VALUES (${order.orderId}, ${productId});`);
}
