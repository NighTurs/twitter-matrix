(function () {
    "use strict";
    $(document).ready(function () {
        var MAX_TWEET_LENGTH = 140;
        var CELL_SIZE_MIN = 5;
        var CELL_SIZE_MAX = 10;
        var MAX_PENDING_TWEETS = 200;
        var BCOLOR = '#000';
        var NEW_CHAR_COLOR = '#0F0';
        var FONT = 'Courier New';
        var WS_URL = "ws://" + location.hostname + ":15674/ws";
        var DRAW_INTERVAL = 33;

        // application state
        var st = {
            cellHeight: null,
            cellWidth: null,
            font: FONT,
            width: 0,
            height: 0,
            gridn: 0,
            gridm: 0,
            // Container of matrix cells with references to currently displayed tweets
            grid: [],
            // Displayed tweets by key
            shownTweets: new Map(),
            // Tweet key that is currently hovered over
            hoverTweetKey: null,
            // Each separate row to display tweets
            rollers: [],
            // Tweet phrases by id
            phrases: new Map(),
            // Set of phrases to filter out
            filterPhrases: new Set(),
            pendingTweets: []
        };

        var ctxover = canvover.getContext('2d');
        var ctx = canv.getContext('2d');

        // initialize app state
        adjustToNewWindowSize();

        var drawMatrix = function () {
            distributePendingTweets();

            // shade for older text
            ctx.fillStyle = 'rgba(0,0,0,.01)';
            ctx.fillRect(0, 0, st.width, st.height);
            // setup to type new characters
            ctx.font = st.font;

            // each roller will type new character
            for (var i = 0; i < st.gridn; i++) {
                var roller = st.rollers[i];
                if (roller.tweetQueue.length > 0) {
                    var cell = st.grid[i][roller.cellInd];
                    var topTweet = roller.tweetQueue[0];
                    var topTweetText = topTweet.text;
                    var topTweetKey = topTweet.id;
                    ctx.fillStyle = BCOLOR;
                    ctx.fillRect(cell.x, cell.y - st.cellHeight, st.cellWidth, st.cellHeight);
                    ctx.fillStyle = NEW_CHAR_COLOR;
                    ctx.fillText(topTweetText[roller.curTweetPos], cell.x, cell.y);

                    var tweetInfo;
                    // cell will no longer reference previous tweet
                    if (cell.tweetKey) {
                        tweetInfo = st.shownTweets.get(cell.tweetKey);
                        if (tweetInfo.count <= 1) {
                            // tweet lost his last reference, remove it
                            st.shownTweets.delete(cell.tweetKey);
                        } else {
                            tweetInfo.count--;
                        }
                    }
                    cell.tweetKey = topTweetKey;
                    if (st.shownTweets.has(topTweetKey)) {
                        tweetInfo = st.shownTweets.get(topTweetKey);
                        tweetInfo.count++;
                        tweetInfo.textOffset = roller.curTweetPos;
                    } else {
                        st.shownTweets.set(topTweetKey, {
                            stGridI: i,
                            stGridH: roller.cellInd,
                            url: topTweet.url,
                            text: topTweetText,
                            textOffset: roller.curTweetPos,
                            count: 1
                        });
                    }

                    roller.curTweetPos++;
                    if (roller.curTweetPos >= topTweetText.length) {
                        roller.curTweetPos = 0;
                        roller.tweetQueue.shift();
                    }
                    roller.cellInd++;
                    if (roller.cellInd >= st.gridm) {
                        roller.cellInd = 0;
                    }
                }
            }

            // highlight hovered tweet
            ctxover.clearRect(0, 0, st.width, st.height);
            ctxover.font = st.font;
            if (st.hoverTweetKey && st.shownTweets.has(st.hoverTweetKey)) {
                tweetInfo = st.shownTweets.get(st.hoverTweetKey);
                var gi = tweetInfo.stGridI;
                var gh = tweetInfo.stGridH;
                for (i = 0; i <= tweetInfo.textOffset; i++) {
                    cell = st.grid[gi][gh];
                    ctxover.clearRect(cell.x, cell.y - st.cellHeight, st.cellWidth, st.cellHeight);
                    ctxover.fillStyle = '#FFF';
                    ctxover.fillText(tweetInfo.text[i], cell.x, cell.y);
                    gh++;
                    if (gh >= st.gridm) {
                        gh = 0;
                    }
                }
            }
        };

        function distributePendingTweets() {
            var i;
            var vacantRollers = [];
            if (st.pendingTweets.length == 0) {
                return;
            }
            for (i = 0; i < st.rollers.length; i++) {
                if (st.rollers[i].tweetQueue.length == 0) {
                    vacantRollers.push(i);
                }
            }
            // to avoid rollers moving in static pattern under high load
            // introduce random delay before new tweet is assigned to vacant roller
            var coinFlip = Math.floor(Math.random() * vacantRollers.length * 1.5);
            if (coinFlip < vacantRollers.length) {
                // filter out tweets
                while (st.pendingTweets.length > 0 && filterTweet(st.pendingTweets[st.pendingTweets.length - 1])) {
                    st.pendingTweets.pop();
                }
                if (st.pendingTweets.length > 0) {
                    st.rollers[vacantRollers[coinFlip]].tweetQueue.push(st.pendingTweets.pop());
                }
            }
            // remove excess of pending tweets
            if (st.pendingTweets.length > MAX_PENDING_TWEETS) {
                st.pendingTweets = st.pendingTweets.slice(-(MAX_PENDING_TWEETS >> 1))
            }
        }

        function filterTweet(tweet) {
            var filter = true;
            tweet.phrases.forEach(function (entry) {
                filter &= st.filterPhrases.has(entry);
            });
            return filter;
        }

        // When tab is inactive intervals are triggered only once per second,
        // so there is need to make up for missed calls
        function smartInterval(func, interval) {
            var last = new Date() - interval;
            var now;
            var numMissed;

            (function iterate() {
                func();
                now = +new Date();
                numMissed = Math.round((now - last) / interval) - 1;
                while (numMissed--) {
                    func();
                }
                last = +new Date();
                setTimeout(iterate, interval);
            })();
        }

        // run matrix animation
        smartInterval(drawMatrix, DRAW_INTERVAL);

        function adjustToNewWindowSize() {
            var i, h;

            //adjust canvas size
            st.width = canv.width = canvover.width = window.innerWidth;
            st.height = canv.height = canvover.height = window.innerHeight;

            // pick cell width and font to tweets without overlapping
            st.cellWidth = Math.min(Math.max(
                Math.floor(st.width / MAX_TWEET_LENGTH), CELL_SIZE_MIN), CELL_SIZE_MAX);
            //noinspection JSSuspiciousNameCombination
            st.cellHeight = st.cellWidth;
            st.font = Math.floor(st.cellWidth * 1.5) + 'px ' + FONT;

            st.gridn = Math.floor((st.height - st.cellHeight) / st.cellHeight) + 1;
            st.gridm = Math.floor((st.width - st.cellWidth) / st.cellWidth) + 1;

            // black background
            ctx.fillStyle = BCOLOR;
            ctx.fillRect(0, 0, st.width, st.height);
            // transparent second layer
            ctxover.clearRect(0, 0, st.width, st.height);

            //reset hovered tweet
            st.hoverTweetKey = null;

            //reinitialize grid
            st.grid = [];
            for (i = st.cellHeight; i <= st.height; i += st.cellHeight) {
                var row = [];
                for (h = 0; h <= st.width - st.cellWidth; h += st.cellWidth) {
                    row.push({
                        x: h,
                        y: i,
                        tweetKey: null
                    })
                }
                st.grid.push(row);
            }

            // reinitialize rollers
            st.rollers = [];
            for (i = 0; i < st.gridn; i++) {
                st.rollers.push({
                    tweetQueue: [],
                    curTweetPos: 0,
                    cellInd: 0
                })
            }

            // reinitialize shown tweets map
            st.shownTweets = new Map();
        }

        // Upon tweet click open it's page
        canvover.addEventListener('click', function (e) {
            var cell = mouseEventToGridCell(this, e);
            if (cell && cell.tweetKey) {
                window.open(st.shownTweets.get(cell.tweetKey).url);
            }
        });

        canvover.addEventListener('mouseout', function () {
            st.hoverTweetKey = null;
        });

        canvover.addEventListener('mousemove', function (e) {
            var cell = mouseEventToGridCell(this, e);
            st.hoverTweetKey = cell ? cell.tweetKey : null;
        });

        function mouseEventToGridCell(ref, e) {
            var clickedX = e.pageX - ref.offsetLeft;
            var clickedY = e.pageY - ref.offsetTop;
            var gridI = Math.floor(clickedY / st.cellHeight);
            var gridH = Math.floor(clickedX / st.cellWidth);
            if (gridI < st.gridn && gridH < st.gridm) {
                return st.grid[gridI][gridH];
            } else {
                return null;
            }
        }

        function phraseId(phrase) {
            return "cb_" + phrase.replace(new RegExp(' ', 'g'), '_');
        }

        $(document).on('change', '[type=checkbox]', function (e) {
            var cb = $(e.target);
            var label = $(e.target).parent();
            var phrase = st.phrases.get(label.attr('id')).phrase;
            if (!cb.is(':checked')) {
                st.filterPhrases.add(phrase);
            } else {
                st.filterPhrases.delete(phrase);
            }
        });

        $(window).resize(function () {
            adjustToNewWindowSize()
        });

        var client = Stomp.client(WS_URL, []);
        client.debug = null;

        client.connect('guest', 'guest', function () {
            client.subscribe("/exchange/twitter.tweet",
                function (message) {
                    var tweet = JSON.parse(message.body);
                    if (!filterTweet(tweet)) {
                        st.pendingTweets.push(tweet);
                    }
                },
                {priority: 9}
            );
            client.subscribe("/exchange/twitter.tweet.phrases",
                function (message) {
                    var msg = JSON.parse(message.body);
                    var curPhrases = new Map();
                    msg.phrases.forEach(function (entry) {
                        var key = phraseId(entry.phrase);
                        curPhrases.set(key, {
                            phrase: entry.phrase,
                            freqMinute: entry.stats.freqMinute
                        });
                    });
                    // remove phrases that disappeared
                    st.phrases.forEach(function (value, key) {
                        if (!curPhrases.has(key)) {
                            $('#' + key).remove();
                            st.filterPhrases.delete(value);
                        }
                    });
                    // and new phrases and update existing
                    curPhrases.forEach(function (value, key) {
                        var phraseSelector = '#' + key;
                        if ($(phraseSelector).length) {
                            $(phraseSelector).contents().filter(function () {
                                    return this.nodeType == 3;
                                })
                                .last().replaceWith(value.freqMinute + ' ' + value.phrase);
                        } else {
                            $("#phrases").append('<label id="' + key + '" class="btn btn-switch active"> \
                        <input type="checkbox" checked> ' + value.freqMinute + ' ' + value.phrase + '</label>')
                        }
                    });
                    st.phrases = curPhrases;
                },
                {priority: 9}
            );
        });
    });
})();