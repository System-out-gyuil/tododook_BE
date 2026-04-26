import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "1m", target: 20 },
    { duration: "2m", target: 50 },
    { duration: "2m", target: 100 },
    { duration: "2m", target: 200 },
    { duration: "1m", target: 0 },  // cooldown
  ]
};

export default function () {
  const res = http.get("https://api.tododook.com/api/v1/categories/test");
  check(res, {
    "status is 200":       (r) => r.status === 200,
    "duration < 500ms":    (r) => r.timings.duration < 500,
  });
  sleep(1);
}