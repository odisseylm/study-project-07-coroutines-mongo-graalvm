
// See all roles at
// https://www.mongodb.com/docs/manual/reference/built-in-roles/#std-label-built-in-roles


print("### 00 \n\n");

const adminUsername = "admin123";
const adminPsw = "admin123";

print("### 01 \n\n");

// use admin;
db = db.getSiblingDB("admin");

print(`### 02  createUser("${adminUsername}") \n\n`);

db.createUser({ user: adminUsername,  pwd: adminPsw,  roles: [ "root" ] })


// print("### 03 \n\n");
// db = db.getSiblingDB("admin");
// print("### 04 \n\n");
// // db.createUser({ user: "db1user",  pwd: "db1psw",  roles: [ "readWrite" ] })
// db.createUser({ user: "db1user",  pwd: "db1psw",  roles: [ { role: "readWrite", db: "db1" } ] })


const db1UsersAuthDb = "db1" // or "admin" (I don't know which approach is better.)

print("### 05 (1) \n\n");
db = db.getSiblingDB(db1UsersAuthDb);

print('### 05 (1) create "db1" users \n\n');

print('### 05 (2) createUser("db1user") \n\n');
db.createUser({ user: "db1user",  pwd: "db1psw",  roles: [ { role: "readWrite", db: "db1" } ] })
// db.createUser({ user: "db1user",  pwd: "db1psw",  roles: ["readWrite"] })


print('### 05 (3) createUser("db1guest") \n\n');
// db.createUser({ user: "db1guest", pwd: "db1psw",  roles: [ { role: "read", db: "db1" } ] })
db.createUser({ user: "db1guest", pwd: "db1psw",  roles: ["read"] })


print('### 05 (4) createUser("db1admin") \n\n');
// db.createUser({ user: "db1admin",  pwd: "db1psw",  roles: [
//           { role: "readWrite", db: "db1" }
//         , { role: "dbAdmin",   db: "db1" }
//         , { role: "userAdmin", db: "db1" }
//         // or dbOwner = readWrite & dbAdmin & userAdmin
//         //, { role: "dbOwner", db: "db1" }
//     ] })
db.createUser({ user: "db1admin",  pwd: "db1psw",
    roles: [ "readWrite", "dbAdmin", "userAdmin" ] }) // or dbOwner = readWrite & dbAdmin & userAdmin




//------------------------------------------------------------
print("### END of users \n\n");



// mongod --port 27017 --dbpath /data/db --auth
//
// Or if you are deploying MongoDB using a config file, you need to include:
//
// security:
//    authorization: "enabled"
