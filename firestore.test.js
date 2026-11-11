const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "astronaut_alice";
const BOB_UID = "astronaut_bob";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot write profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    email: "alice@nasa.gov",
    fullName: "Alice Vance",
    adminGivenId: "NASA-AS-701",
    role: "crew",
    createdAt: new Date(),
  }));
});

test("Authenticated user: can create their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    email: "alice@nasa.gov",
    fullName: "Alice Vance",
    adminGivenId: "NASA-AS-701",
    role: "crew",
    createdAt: new Date(),
  }));
});

test("Authenticated user: cannot forge another astronaut's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    email: "bob@nasa.gov",
    fullName: "Bob Vance",
    adminGivenId: "NASA-AS-702",
    role: "crew",
    createdAt: new Date(),
  }));
});

test("Authenticated user: can add own telemetry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("telemetry").add({
    userId: ALICE_UID,
    heartRate: 72,
    radiationCumulativeMsv: 0.45,
    timestamp: new Date(),
  }));
});

test("Authenticated user: cannot inject telemetry into another user's stream", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("telemetry").add({
    userId: BOB_UID,
    heartRate: 72,
    radiationCumulativeMsv: 0.45,
    timestamp: new Date(),
  }));
});

test("Crew message: authenticated sender can send valid message", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("messages").doc("msg_1").set({
    id: "msg_1",
    senderId: ALICE_UID,
    senderName: "Alice",
    recipientId: "ALL_CREW",
    message: "Solar flare watch initiated",
    timestamp: new Date(),
  }));
});
