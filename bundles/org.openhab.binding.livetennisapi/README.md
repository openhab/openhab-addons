Single-line edit (applied and built). Replace the pagination paragraph so it no longer claims the snapshot always continues until complete:

OLD:
The live match list is paginated. The bridge reads the `meta.has_more` flag and pages forward until the snapshot is complete, so more than 200 concurrent live matches are not silently dropped; in practice the whole live board is well under one page, so this remains a single request per cycle.

NEW:
The live match list is paginated. The bridge reads the `meta.has_more` flag and pages forward (200 matches per page) up to a defensive cap of five pages, so more than 200 concurrent live matches are not silently dropped. If the API still reports more matches at that cap, the snapshot for that cycle is deliberately truncated at 1000 matches and the truncation is logged as a warning rather than presented as complete. In practice the whole live board is well under one page, so this remains a single request per cycle.