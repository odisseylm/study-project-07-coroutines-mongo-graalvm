
//use db1;
db = db.getSiblingDB("db1");

// predefined
db.users.insertOne({_id: ObjectId("000000000000000000000001"), name: "user1" })
db.users.insertOne({_id: ObjectId("000000000000000000000002"), name: "user2" })

