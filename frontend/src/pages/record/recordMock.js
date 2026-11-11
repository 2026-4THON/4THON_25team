import poster1 from "../../assets/record/poster-sample-1.png";
import poster2 from "../../assets/record/poster-sample-2.png";

export const RECORDS = [
  {
    id: 3,
    no: 3,
    genre: "뮤지컬",
    title: "팬레터",
    date: "2026.04.11",
    time: "18:30",
    venue: "홍익대 아트센터 대극장",
    poster: poster1,
  },
  {
    id: 2,
    no: 2,
    genre: "뮤지컬",
    title: "브라더스 까라마조프",
    date: "2026.04.11",
    time: "18:30",
    venue: "NOL 씨어터 대학로...",
    poster: poster2,
  },
  {
    id: 1,
    no: 1,
    genre: "기타",
    title: "제목",
    date: "2026.04.11",
    time: "18:30",
    venue: "극장이름~~~",
    poster: poster1,
  },
];

export const RECORD_DETAIL = {
  id: 3,
  genre: "뮤지컬",
  title: "팬레터",
  date: "2026.04.11",
  time: "18:30",
  venue: "홍익대 아트센터 대극장",
  runningTime: "180분",
  seat: ["1층", "1열", "15번"],
  companion: "친구",
  cast: ["전동석", "이봄소리", "규현", "박지연", "이지혜", "박은태", "김준수", "유승현"],
  expenses: [
    { label: "티켓", amount: "34,000원" },
    { label: "청년 문화 예술 패스", amount: "30,000원" },
    { label: "MD", amount: "20,000원" },
  ],
  total: "24000원",
  rating: 4,
  review:
    "되게 신선한 스토리였다!\n들을때는 크게 흥얼거릴정도의 넘버는 없었는데 집에와서 계속 곱씹게되는 넘버가 많은 극인듯\n감정선이 되게 묘하면서 슬픈 스토리임\n처음 뮤지컬 접하면 이해하기 조금 어려울수도 ㅎㅎ",
  pairActors: ["전동석", "박은태", "규현"],
  pairReview:
    "A 배우가 해진 특유의 진지하고 예민한 분위기를 살려 대사를 이어가는데, B 배우가 세훈 특유의 순수하고 어딘가 엉뚱한 반응으로 받아치는 장면이 너무 좋았다. 특히 B 배우가 대사 끝에 살짝 덧붙인 애드리브에 A 배우가 순간 웃음을 참는 듯한 표정을 지었는데, 그걸 놓치지 않고 C 배우가 자연스럽게 끼어들면서 이어지는 티키타카가 정말 재미있었다",
  actors: ["전동석", "박은태", "규현", "유승현", "이지혜", "김준수"],
  actorReviews: {
    전동석:
      "해진 특유의 섬세하고 예민한 감정선을 정말 잘 살려주셨다.\n특히 세훈을 대하는 태도에서 느껴지는 애정과 경계심이 동시에 표현되는 게 너무 좋았음.. 후반부로 갈수록 무너져 내리는 감정 연기가 인상적이었고, 넘버마다 감정이 조금씩 달라지는 것도 좋았다. 마지막 장면은 정말 한동안 여운이 가시지 않았다.\n목소리 톤이 묵직하셔서 그런지 더욱더 몰입됐던!",
  },
};
