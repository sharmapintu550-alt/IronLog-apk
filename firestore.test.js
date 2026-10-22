const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

// --- SECURITY BOUNDS & ISOLATION TESTS ---

test("Unauthenticated user: cannot read user documents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot write workout sessions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w1").set({
      id: "w1",
      userId: ALICE_UID,
      routineName: "Chest & Triceps",
      startTime: 1000,
      endTime: 2000,
      durationMinutes: 60,
      totalVolumeKg: 5000,
      totalSets: 12,
      notes: "",
      isCompleted: true,
      updatedAt: 2500,
    })
  );
});

test("Authenticated user: cannot read another user's workout sessions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("workouts").doc("bob_w1").set({
      id: "bob_w1",
      userId: BOB_UID,
      routineName: "Leg Day",
      updatedAt: 1234,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("workouts").doc("bob_w1").get()
  );
});

test("Authenticated user: cannot write another user's custom exercises", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("custom_exercises").doc("ex1").set({
      id: "ex1",
      userId: BOB_UID,
      name: "Incline DB Press",
      updatedAt: 1000,
    })
  );
});

test("Authenticated user: can write and read their own workout data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const workoutRef = aliceDb.collection("users").doc(ALICE_UID).collection("workouts").doc("alice_w1");

  await assertSucceeds(
    workoutRef.set({
      id: "alice_w1",
      userId: ALICE_UID,
      routineName: "Push Day",
      startTime: 1000,
      endTime: 2000,
      durationMinutes: 45,
      totalVolumeKg: 3500,
      totalSets: 10,
      notes: "Felt strong",
      isCompleted: true,
      updatedAt: 2100,
    })
  );

  await assertSucceeds(workoutRef.get());
});

test("Authenticated user: can write and read sets under their workout", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const setRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("workouts")
    .doc("alice_w1")
    .collection("sets")
    .doc("set_1");

  await assertSucceeds(
    setRef.set({
      id: "set_1",
      sessionId: "alice_w1",
      exerciseId: "ex_bench",
      exerciseName: "Bench Press",
      setNumber: 1,
      setType: "NORMAL",
      weightKg: 100,
      reps: 5,
      rpe: 8.5,
      isCompleted: true,
      completedAt: 1500,
      updatedAt: 1500,
    })
  );

  await assertSucceeds(setRef.get());
});

test("Authenticated user: can write body measurements", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const measureRef = aliceDb.collection("users").doc(ALICE_UID).collection("measurements").doc("m1");

  await assertSucceeds(
    measureRef.set({
      id: "m1",
      userId: ALICE_UID,
      timestamp: 12345,
      dateString: "Oct 6, 2026",
      weightKg: 82.5,
      targetWeightKg: 80.0,
      updatedAt: 12345,
    })
  );

  await assertSucceeds(measureRef.get());
});
