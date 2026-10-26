const dbName = process.env.MONGO_APP_DATABASE;
const appUser = process.env.MONGO_APP_USERNAME;
const appPassword = process.env.MONGO_APP_PASSWORD;

const appDb = db.getSiblingDB(dbName);

appDb.createUser({
    user : appUser,
    pwd : appPassword,
    roles : [{ role: "readWrite", db: dbName }]
});

appDb.createCollection( 'rondas');
appDb.createCollection('agendamentosronda');

appDb.rondas.createIndex(
  { ativa: 1 },
  { name: 'idx_rondas_ativa' }
);
 
appDb.agendamentosronda.createIndex(
  { responsavel: 1, status: 1 },
  { name: 'idx_agendamentosronda_responsavel_status' }
);
 
appDb.agendamentosronda.createIndex(
  { idRonda: 1, status: 1 },
  { name: 'idx_agendamentosronda_ronda_status' }
);
 
print(`[init] banco '${dbName}' pronto: usuario '${appUser}', collections e indices criados.`);