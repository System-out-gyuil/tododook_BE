import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "1m", target: 20 },
    { duration: "2m", target: 50 },
    { duration: "2m", target: 100 },
    { duration: "2m", target: 200 },
  ]

};


export default function () {
  http.get("https://api.tododook.com/api/v1/categories/test");
  sleep(1);
}