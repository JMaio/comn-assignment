stats = {
    25: {
        1: [
            "7.660186",
            "7.756061",
            "7.779769",
            "7.728551",
            "7.403023",
        ],
        2: [
            "10.008032",
            "10.475707",
            "10.493358",
            "10.610904",
            "9.825276",
        ],
        4: [
            "13.980842",
            "16.029184",
            "15.230686",
            "14.876658",
            "14.910761",
        ],
        8: [
            "24.481967",
            "22.599854",
            "22.342809",
            "23.443992",
            "22.346220",
        ],
        16: [
            "36.199642",
            "37.391191",
            "35.263432",
            "34.759509",
            "35.359994",
        ],
        32: [
            "60.625154",
            "54.550210",
            "55.688402",
            "64.075797",
            "55.617857",
        ],
    },
}

for (tx_k, tx_vs) in stats.items():
    print(tx_k)
    for (ws_k, ws_v) in tx_vs.items():
        r = [float(x) for x in ws_v]
        print(f"{round(sum(r) / len(r), 2)}")
    print()
