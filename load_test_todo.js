import http from "k6/http";
import { check } from "k6";

export const options = {
  stages: [
    { duration: "1m", target: 20 }, // 1분 동안 가상 사용자 수 20명까지 증가
    { duration: "1m", target: 20 }, // 1분 동안 20명 유지
    { duration: "1m", target: 50 }, // 1분 동안 50명까지 증가
    { duration: "1m", target: 50 }, // 1분 동안 피크 유지
  ],

};


export default function () {
  http.get("http://localhost:8090/api/v1/todos?categoryId=1");
  sleep(1);
}