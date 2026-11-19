const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const OWNER_UID = "owner_alansari";
const OWNER_EMAIL = "amhmd11225@gmail.com";
const USER_A_UID = "viewer_alice";
const USER_B_UID = "viewer_bob";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validChannelConfigPayload(uid, overrides = {}) {
  return {
    primaryLiveUrl: "rtmp://live.alansaritv.example/app/main",
    backupLiveUrl: "srt://srt.alansaritv.example:9000?streamid=alansari",
    liveTitle: "البث المباشر لقناة الأنصاري",
    youtubeHandle: "@alansarie1",
    youtubeChannelId: "UCbZnqtO8B64mMhTzTXIxqUA",
    facebookUrl: "https://www.facebook.com/al.ansar.live",
    telegramUrl: "https://t.me/SNG1080",
    tiktokUrl: "https://www.tiktok.com/@alansarie1",
    instagramUrl: "https://www.instagram.com/alansarie1",
    satelliteFrequency: "وحدة البث الخارجي SNG1080",
    updatedByUid: uid,
    updatedAt: new Date(Date.now() - 1000),
    ...overrides,
  };
}

function validCoveragePayload(uid, overrides = {}) {
  const past = new Date(Date.now() - 2000);
  return {
    userId: uid,
    senderName: "موكب شهيد الجمعة",
    phone: "07801234567",
    governorate: "ذي قار - سوق الشيوخ",
    serviceType: "تغطية وبث مباشر",
    eventDate: "الجمعة 8:00 مساءً",
    topic: "مجلس عزاء مركزي",
    details: "قرب الجسر الرئيسي",
    status: "pending",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

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

test("Channel config: unauthenticated viewer can get 'live' doc, cannot list or write", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("channel_config")
      .doc("live")
      .set(validChannelConfigPayload(OWNER_UID));
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("channel_config").doc("live").get());
  await assertFails(unauthDb.collection("channel_config").get());
  await assertFails(
    unauthDb
      .collection("channel_config")
      .doc("live")
      .update({ primaryLiveUrl: "rtmp://attacker/hijack" })
  );
});

test("Channel config: non-owner signed-in user and unverified email spoof cannot write", async () => {
  const regularDb = testEnv
    .authenticatedContext(USER_A_UID, {
      email: "someone@gmail.com",
      email_verified: true,
    })
    .firestore();

  await assertFails(
    regularDb
      .collection("channel_config")
      .doc("live")
      .set(validChannelConfigPayload(USER_A_UID))
  );

  // Email spoofing test: matching email but email_verified = false
  const spoofDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: OWNER_EMAIL,
      email_verified: false,
    })
    .firestore();

  await assertFails(
    spoofDb
      .collection("channel_config")
      .doc("live")
      .set(validChannelConfigPayload("spoof_uid"))
  );
});

test("Channel config: verified owner can create & update RTMP/SRT/HLS stream, rejects ghost fields", async () => {
  const ownerDb = testEnv
    .authenticatedContext(OWNER_UID, {
      email: OWNER_EMAIL,
      email_verified: true,
    })
    .firestore();

  await assertSucceeds(
    ownerDb
      .collection("channel_config")
      .doc("live")
      .set(validChannelConfigPayload(OWNER_UID))
  );

  await assertSucceeds(
    ownerDb.collection("channel_config").doc("live").update({
      primaryLiveUrl: "srt://sng1080.example.com:9000?mode=caller",
      updatedByUid: OWNER_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Shadow update with ghost field must fail
  await assertFails(
    ownerDb
      .collection("channel_config")
      .doc("live_test")
      .set(validChannelConfigPayload(OWNER_UID, { isVerified: true }))
  );
});

test("Coverage requests: PII isolation, user-scoped query, and owner-only status updates", async () => {
  const userADb = testEnv
    .authenticatedContext(USER_A_UID, {
      email: "alice@gmail.com",
      email_verified: true,
    })
    .firestore();
  const userBDb = testEnv
    .authenticatedContext(USER_B_UID, {
      email: "bob@gmail.com",
      email_verified: true,
    })
    .firestore();
  const ownerDb = testEnv
    .authenticatedContext(OWNER_UID, {
      email: OWNER_EMAIL,
      email_verified: true,
    })
    .firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();

  // Unauthenticated cannot create or read bookings
  await assertFails(
    unauthDb
      .collection("coverage_requests")
      .doc("req_1")
      .set(validCoveragePayload(USER_A_UID))
  );
  await assertFails(unauthDb.collection("coverage_requests").get());

  // User A creates own booking
  await assertSucceeds(
    userADb
      .collection("coverage_requests")
      .doc("req_1")
      .set(validCoveragePayload(USER_A_UID))
  );

  // User A cannot create booking with pre-confirmed status or adminNote
  await assertFails(
    userADb
      .collection("coverage_requests")
      .doc("req_bad")
      .set(validCoveragePayload(USER_A_UID, { status: "confirmed" }))
  );

  // User A filtered query succeeds; unfiltered query fails for non-owner
  await assertSucceeds(
    userADb
      .collection("coverage_requests")
      .where("userId", "==", USER_A_UID)
      .get()
  );
  await assertFails(userADb.collection("coverage_requests").get());

  // User B cannot read or update User A's booking (PII isolation)
  await assertFails(
    userBDb.collection("coverage_requests").doc("req_1").get()
  );
  await assertFails(
    userADb.collection("coverage_requests").doc("req_1").update({
      status: "confirmed",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Verified owner can list all bookings and update status + adminNote
  await assertSucceeds(ownerDb.collection("coverage_requests").get());
  await assertSucceeds(
    ownerDb.collection("coverage_requests").doc("req_1").update({
      status: "confirmed",
      adminNote: "تم تكليف فريق SNG1080",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Future timestamp manipulation rejected
  await assertFails(
    ownerDb.collection("coverage_requests").doc("req_1").update({
      status: "completed",
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});
